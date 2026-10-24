package com.whim.excel.model;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 解析结果，统计成功和失败行数，分批模式不保留成功数据。
 */
@Getter
public final class ExcelResult<T> {

    @Getter(AccessLevel.NONE)
    private final List<T> rows = new ArrayList<>();

    private final List<ExcelError> errors = new ArrayList<>();
    private long successCount;
    private long failureCount;

    /** 记录一条成功数据；分批处理时只计数。 */
    public void addSuccess(T row, boolean retainRows) {
        successCount++;
        if (retainRows) {
            rows.add(row);
        }
    }

    /** 记录一条失败行及其错误描述。 */
    public void addError(ExcelError error) {
        failureCount++;
        errors.add(error);
    }

    /** 获取只读的成功数据列表。 */
    public List<T> getList() {
        return Collections.unmodifiableList(rows);
    }

    /** 获取只读的结构化错误列表。 */
    public List<ExcelError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /** 获取成功与失败数据行的总数。 */
    public long getTotalCount() {
        return successCount + failureCount;
    }

    /** 判断所有已解析数据行是否通过校验。 */
    public boolean isSuccess() {
        return failureCount == 0;
    }

    /** 获取解析统计；解析成功不代表业务事务已经提交。 */
    public String getAnalysis() {
        return "共解析 " + getTotalCount() + " 行，成功 " + successCount + " 行，失败 " + failureCount + " 行";
    }
}
