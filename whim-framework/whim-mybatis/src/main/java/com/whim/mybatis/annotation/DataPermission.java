package com.whim.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * @date 2025/7/4 12:36
 * @description 数据权限注解（支持类/方法级别）
 * value 权限字段规则
 * joinStr 多个条件间的连接逻辑（如 "AND"、"OR"），默认空字符串表示OR
 * 注意：手动拼接时需自行处理SQL安全
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface DataPermission {
    DataColumn[] value();

    String joinStr() default "";
}
