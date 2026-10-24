package com.whim.excel.core;

import com.whim.core.exception.ServiceException;
import com.whim.excel.exception.ExcelImportException;
import com.whim.excel.model.ExcelError;
import com.whim.excel.model.ExcelResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.apache.fesod.sheet.exception.ExcelDataConvertException;
import org.apache.fesod.sheet.metadata.Head;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 统一执行表头校验、逐行校验、错误统计和有界分批回调。
 */
@RequiredArgsConstructor
public final class DefaultExcelListener<T> extends AnalysisEventListener<T> {

    private final Validator validator;
    private final Class<?>[] validationGroups;
    private final boolean validate;
    private final boolean checkHead;
    private final boolean failFast;
    private final boolean retainRows;
    private final long maxRows;
    private final int maxErrors;
    private final int batchSize;
    private final Consumer<List<T>> batchConsumer;
    private final ExcelRowListener<T> rowListener;

    @Getter
    private final ExcelResult<T> result = new ExcelResult<>();

    private final List<T> batch = new ArrayList<>();

    /** 在最后一级表头检查缺失、重复或错位的标题。 */
    @Override
    public void invokeHeadMap(Map<Integer, String> headers, AnalysisContext context) {
        if (!checkHead || context.readRowHolder().getRowIndex() + 1 != context.readSheetHolder().getHeadRowNumber()) {
            return;
        }
        Map<Integer, Head> expected = context.readSheetHolder().getExcelReadHeadProperty().getHeadMap();
        Set<String> names = new HashSet<>();
        for (Head head : expected.values()) {
            List<String> titles = head.getHeadNameList();
            if (titles.isEmpty()) {
                continue;
            }
            String title = titles.getLast();
            if (Boolean.TRUE.equals(head.getForceIndex())) {
                if (!title.equals(headers.get(head.getColumnIndex()))) {
                    throw new ExcelImportException("导入表头不匹配，预期列名：" + title, result);
                }
            } else if (Boolean.TRUE.equals(head.getForceName())) {
                long count = headers.values().stream().filter(title::equals).count();
                if (count != 1 || !names.add(title)) {
                    throw new ExcelImportException("导入表头缺失或重复，请使用正确模板：" + title, result);
                }
            }
        }
    }

    /** 先校验再交给业务处理；分批模式默认不积累完整数据列表。 */
    @Override
    public void invoke(T row, AnalysisContext context) {
        checkRowLimit();
        if (validate) {
            Set<ConstraintViolation<T>> violations = validator.validate(row, validationGroups);
            if (!violations.isEmpty()) {
                String message = violations
                    .stream()
                    .map(violation -> violation.getPropertyPath() + "：" + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("；"));
                addError(context, null, null, message);
                return;
            }
        }
        if (rowListener != null) {
            try {
                rowListener.onRow(row, context);
            } catch (RuntimeException exception) {
                throw new ServiceException(
                    "Excel 第 " + (context.readRowHolder().getRowIndex() + 1) + " 行业务处理失败",
                    exception
                );
            }
        }
        if (batchConsumer != null) {
            batch.add(row);
            if (batch.size() == batchSize) {
                flushBatch();
            }
        }
        result.addSuccess(row, retainRows);
    }

    /** 仅将单元格转换失败作为可收集的行错误，业务和系统异常立即向上传递。 */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (!(exception instanceof ExcelDataConvertException conversion)) {
            throw exception;
        }
        checkRowLimit();
        Integer column = conversion.getColumnIndex();
        Head head = context.readSheetHolder().getExcelReadHeadProperty().getHeadMap().get(column);
        String fieldName = head == null ? null : head.getFieldName();
        addError(
            context,
            column == null ? null : column + 1,
            fieldName,
            "单元格格式、数字范围或字典选项不正确" + (fieldName == null ? "" : "（" + fieldName + "）")
        );
    }

    /** 每张表读取完成后提交剩余批次，批次不会跨工作表。 */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flushBatch();
    }

    /** 拒绝超限数据，不静默截断导入内容。 */
    private void checkRowLimit() {
        if (result.getTotalCount() >= maxRows) {
            throw new ExcelImportException("导入数据超过最大允许行数：" + maxRows, result);
        }
    }

    /** 记录一条错误行，按快速失败或错误数量上限停止。 */
    private void addError(AnalysisContext context, Integer column, String fieldName, String message) {
        ExcelError error = new ExcelError(
            context.readSheetHolder().getSheetName(),
            context.readRowHolder().getRowIndex() + 1,
            column,
            fieldName,
            message
        );
        result.addError(error);
        if (failFast || result.getFailureCount() >= maxErrors) {
            throw new ExcelImportException(
                error.getDescription() + (failFast ? "" : "；已达到错误行数上限：" + maxErrors),
                result
            );
        }
    }

    /** 将不可变批次传给业务，业务异常必须中止并保留原因。 */
    private void flushBatch() {
        if (batchConsumer == null || batch.isEmpty()) {
            return;
        }
        try {
            batchConsumer.accept(List.copyOf(batch));
            batch.clear();
        } catch (RuntimeException exception) {
            throw new ServiceException("Excel 分批业务处理失败", exception);
        }
    }
}
