package com.whim.excel.builder;

import com.whim.core.exception.ServiceException;
import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.annotation.ExcelEnumFormat;
import com.whim.excel.convert.ExcelNumberConverter;
import com.whim.excel.core.DefaultExcelListener;
import com.whim.excel.core.ExcelOperationContext;
import com.whim.excel.core.ExcelRowListener;
import com.whim.excel.exception.ExcelImportException;
import com.whim.excel.model.ExcelResult;
import com.whim.excel.service.ExcelService;
import org.apache.fesod.sheet.ExcelReader;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.read.builder.ExcelReaderBuilder;
import org.apache.fesod.sheet.read.metadata.ReadSheet;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 导入构造器，所有终端方法共享同一校验流程，输入流由调用者关闭。
 */
public final class ExcelReadBuilder<T> {

    private final ExcelService service;
    private final InputStream input;
    private final Class<T> headType;
    private final List<Converter<?>> converters = new ArrayList<>();
    private boolean validate = true;
    private boolean checkHead = true;
    private boolean failFast = true;
    private boolean retainRows = true;
    private boolean used;
    private boolean autoTrim = true;
    private Integer sheetNo;
    private String sheetName;
    private Integer headRowNumber;
    private String password;
    private long maxRows;
    private int maxErrors;
    private int batchSize;
    private Class<?>[] validationGroups = new Class<?>[0];
    private Consumer<List<T>> batchConsumer;
    private ExcelRowListener<T> rowListener;

    /** 创建一次性读取器并复制默认限制。 */
    public ExcelReadBuilder(ExcelService service, InputStream input, Class<T> headType) {
        Assert.notNull(input, "Excel 输入流不能为空");
        Assert.notNull(headType, "Excel 导入类型不能为空");
        this.service = service;
        this.input = input;
        this.headType = headType;
        this.maxRows = service.getProperties().getMaxImportRows();
        this.maxErrors = service.getProperties().getMaxErrors();
        this.batchSize = service.getProperties().getBatchSize();
    }

    /** 设置是否执行 Jakarta Validation 校验。 */
    public ExcelReadBuilder<T> validate(boolean value) {
        this.validate = value;
        return this;
    }

    /** 设置是否严格检查最后一级表头，默认开启。 */
    public ExcelReadBuilder<T> checkHead(boolean value) {
        this.checkHead = value;
        return this;
    }

    /** 设置首个数据错误是否终止，关闭时收集错误至上限。 */
    public ExcelReadBuilder<T> failFast(boolean value) {
        this.failFast = value;
        return this;
    }

    /** 设置是否保留成功数据，回调模式默认关闭。 */
    public ExcelReadBuilder<T> retainRows(boolean value) {
        this.retainRows = value;
        return this;
    }

    /** 设置是否去除字符串首尾空白。 */
    public ExcelReadBuilder<T> autoTrim(boolean value) {
        this.autoTrim = value;
        return this;
    }

    /** 按从零开始的工作表编号读取。 */
    public ExcelReadBuilder<T> sheetNo(int value) {
        Assert.isTrue(value >= 0, "工作表编号不能小于零");
        this.sheetNo = value;
        return this;
    }

    /** 按工作表名称读取，同时指定编号时两者必须匹配。 */
    public ExcelReadBuilder<T> sheetName(String value) {
        Assert.hasText(value, "工作表名称不能为空");
        this.sheetName = value;
        return this;
    }

    /** 指定表头行数，为零时按列索引映射且不检查表头。 */
    public ExcelReadBuilder<T> headRowNumber(int value) {
        Assert.isTrue(value >= 0, "表头行数不能小于零");
        this.headRowNumber = value;
        return this;
    }

    /** 设置加密文件的读取密码。 */
    public ExcelReadBuilder<T> password(String value) {
        this.password = value;
        return this;
    }

    /** 设置累计数据行上限，超出时抛出包含已有回执的异常。 */
    public ExcelReadBuilder<T> maxRows(long value) {
        Assert.isTrue(value > 0, "最大读取行数必须大于零");
        this.maxRows = value;
        return this;
    }

    /** 设置最多收集的错误行数。 */
    public ExcelReadBuilder<T> maxErrors(int value) {
        Assert.isTrue(value > 0, "最大错误行数必须大于零");
        this.maxErrors = value;
        return this;
    }

    /** 设置 Jakarta Validation 校验分组。 */
    public ExcelReadBuilder<T> validationGroups(Class<?>... groups) {
        this.validationGroups = groups.clone();
        return this;
    }

    /** 注册字段转换器，后注册的同类型转换器优先。 */
    public ExcelReadBuilder<T> registerConverter(Converter<?> converter) {
        Assert.notNull(converter, "Excel 转换器不能为空");
        converters.add(converter);
        return this;
    }

    /** 配置有效行回调，保留统一校验流程，默认不积累成功数据。 */
    public ExcelReadBuilder<T> listener(ExcelRowListener<T> listener) {
        Assert.notNull(listener, "Excel 行监听器不能为空");
        this.rowListener = listener;
        this.retainRows = false;
        return this;
    }

    /** 按默认批量大小处理通过校验的数据。 */
    public ExcelReadBuilder<T> batch(Consumer<List<T>> consumer) {
        return batch(batchSize, consumer);
    }

    /** 配置分批业务回调，默认不保留完整数据，事务由业务服务管理。 */
    public ExcelReadBuilder<T> batch(int size, Consumer<List<T>> consumer) {
        Assert.isTrue(size > 0, "导入批量大小必须大于零");
        Assert.notNull(consumer, "Excel 批量处理器不能为空");
        this.batchSize = size;
        this.batchConsumer = consumer;
        this.retainRows = false;
        return this;
    }

    /** 读取指定工作表，未指定时读取第一张可见表。 */
    public ExcelResult<T> doRead() {
        return read(false);
    }

    /** 读取所有可见表，跳过模板下拉数据所在的隐藏表。 */
    public ExcelResult<T> doReadAll() {
        Assert.isTrue(sheetNo == null && sheetName == null, "读取所有工作表时不能同时指定工作表");
        return read(true);
    }

    /** 使用相同校验流程读取列表；列表模式不允许静默丢弃错误行或配置业务回调。 */
    public List<T> doReadSync() {
        checkListMode();
        ExcelResult<T> result = doRead();
        if (!result.isSuccess()) {
            throw new ExcelImportException(result.getAnalysis(), result);
        }
        return result.getList();
    }

    /** 使用相同校验流程读取所有可见表的数据列表。 */
    public List<T> doReadAllSync() {
        checkListMode();
        ExcelResult<T> result = doReadAll();
        if (!result.isSuccess()) {
            throw new ExcelImportException(result.getAnalysis(), result);
        }
        return result.getList();
    }

    /** 防止列表返回方式与有业务副作用的回调配置混用。 */
    private void checkListMode() {
        Assert.state(
            retainRows && batchConsumer == null && rowListener == null,
            "返回数据列表时不能设置回调或关闭数据保留，请使用 doRead 获取解析回执"
        );
    }

    /** 创建独立解析器，统一管理读取配置、资源和异常传播。 */
    private ExcelResult<T> read(boolean allSheets) {
        Assert.state(!used, "Excel 读取构造器只能执行一次");
        used = true;
        return service.execute(() -> {
            DefaultExcelListener<T> listener = new DefaultExcelListener<>(
                service.getValidator(),
                validationGroups,
                validate,
                checkHead,
                failFast,
                retainRows,
                maxRows,
                maxErrors,
                batchSize,
                batchConsumer,
                rowListener
            );
            ExcelReaderBuilder builder = FesodSheet.read(input, headType, listener)
                .autoCloseStream(false)
                .autoTrim(autoTrim)
                .ignoreEmptyRow(true);
            if (headRowNumber != null) {
                builder.headRowNumber(headRowNumber);
            }
            if (password != null) {
                builder.password(password);
            }
            for (Class<?> type : List.of(Long.class, long.class, BigInteger.class, BigDecimal.class)) {
                builder.registerConverter(new ExcelNumberConverter(type, CellDataTypeEnum.STRING));
                builder.registerConverter(new ExcelNumberConverter(type, CellDataTypeEnum.NUMBER));
            }
            converters.forEach(builder::registerConverter);
            try (ExcelReader reader = builder.build()) {
                // 在处理数据前加载映射，字典查询或配置失败不能被当作普通单元格错误跳过。
                var headProperty = reader.analysisContext().readWorkbookHolder().getExcelReadHeadProperty();
                for (Head head : headProperty.getHeadMap().values()) {
                    Field field = head.getField();
                    if (
                        field != null &&
                        (field.isAnnotationPresent(ExcelDictFormat.class) ||
                            field.isAnnotationPresent(ExcelEnumFormat.class))
                    ) {
                        ExcelOperationContext.current().getMapping(field);
                    }
                }
                List<ReadSheet> sheets = reader
                    .excelExecutor()
                    .sheetList()
                    .stream()
                    .filter(sheet -> allSheets || sheetNo == null || sheetNo.equals(sheet.getSheetNo()))
                    .filter(sheet -> allSheets || sheetName == null || sheetName.equals(sheet.getSheetName()))
                    .filter(
                        sheet -> sheetNo != null || sheetName != null || (!sheet.isHidden() && !sheet.isVeryHidden())
                    )
                    .toList();
                if (sheets.isEmpty()) {
                    throw new ExcelImportException("未找到符合条件的工作表", listener.getResult());
                }
                reader.read(allSheets ? sheets : List.of(sheets.getFirst()));
                return listener.getResult();
            } catch (ExcelImportException | ServiceException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                    if (cause instanceof ExcelImportException importException) {
                        throw importException;
                    }
                    if (cause instanceof ServiceException serviceException) {
                        throw serviceException;
                    }
                }
                throw new IllegalArgumentException("读取 Excel 失败，请检查文件格式、密码和导入字段类型", exception);
            }
        });
    }
}
