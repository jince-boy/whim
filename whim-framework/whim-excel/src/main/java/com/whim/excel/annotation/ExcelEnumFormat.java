package com.whim.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 枚举编码或枚举字段与显示文本之间的映射。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelEnumFormat {
    /** 提供选项的枚举类型。 */
    Class<? extends Enum<?>> enumClass();

    /** 枚举的编码属性，需要提供 getter。 */
    String codeField() default "code";

    /** 枚举的显示文本属性，需要提供 getter。 */
    String textField() default "text";

    /** 是否生成单选下拉。 */
    boolean dropdown() default true;
}
