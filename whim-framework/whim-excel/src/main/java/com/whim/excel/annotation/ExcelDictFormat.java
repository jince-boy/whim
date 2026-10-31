package com.whim.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 固定值映射与下拉选项，转换时配合 ExcelMappingConverter 使用，不查询业务字典。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDictFormat {
    /**
     * 配置固定值与显示文本的映射，例如 0=停用,1=启用；键值本身不能包含逗号。
     *
     * @return 固定值映射表达式
     */
    String readConverterExp();

    /** 多值字段的分隔符。 */
    String separator() default ",";

    /** 是否作为单选下拉；多值字段应关闭。 */
    boolean dropdown() default true;
}
