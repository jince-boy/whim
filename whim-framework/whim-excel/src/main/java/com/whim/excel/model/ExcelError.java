package com.whim.excel.model;

import lombok.Value;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 结构化导入错误，行列号从一开始，无法定位列时列号为空。
 */
@Value
public class ExcelError {

    String sheetName;
    int rowNumber;
    Integer columnNumber;
    String fieldName;
    String message;

    /** 生成人可读的错误描述，不包含 HTML 或原始敏感单元格值。 */
    public String getDescription() {
        return (
            "工作表「" +
            sheetName +
            "」第 " +
            rowNumber +
            " 行" +
            (columnNumber == null ? "" : "第 " + columnNumber + " 列") +
            "：" +
            message
        );
    }
}
