package com.whim.excel.exception;

import com.whim.excel.model.ExcelResult;
import lombok.Getter;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 导入失败或超出限制，复用项目非法参数异常映射并保留已解析结果。
 */
@Getter
public final class ExcelImportException extends IllegalArgumentException {

    private final ExcelResult<?> result;

    /** 创建包含解析回执的异常。 */
    public ExcelImportException(String message, ExcelResult<?> result) {
        super(message);
        this.result = result;
    }
}
