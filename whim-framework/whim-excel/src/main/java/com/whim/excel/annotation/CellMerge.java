package com.whim.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 按原始字段值合并相邻非空单元格，分批写入时保持连续分组。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CellMerge {
    /** 必须同时相等的字段名；依赖字段应包含在当前导出列中。 */
    String[] mergeBy() default {};
}
