package com.whim.excel.core;

import org.apache.fesod.sheet.context.AnalysisContext;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 经过转换和校验后的逐行业务回调，异常会终止整个导入。
 */
@FunctionalInterface
public interface ExcelRowListener<T> {
    /** 处理有效行，context 可获取工作表与实际行号。 */
    void onRow(T row, AnalysisContext context);
}
