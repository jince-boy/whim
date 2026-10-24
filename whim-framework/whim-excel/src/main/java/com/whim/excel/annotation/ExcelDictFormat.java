package com.whim.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典双向转换与下拉选项，转换时配合 ExcelMappingConverter 使用。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDictFormat {
    /** 系统字典类型，与固定表达式二选一。 */
    String dictType() default "";

    /** 固定表达式，例如 0=停用,1=启用；键值本身不能包含逗号。 */
    String readConverterExp() default "";

    /** 多值字段的分隔符。 */
    String separator() default ",";

    /** 是否作为单选下拉；多值字段应关闭。 */
    boolean dropdown() default true;
}
