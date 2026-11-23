package com.whim.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/04/08
 * @description 标记跳过全局 XSS 过滤的控制器或处理方法。
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XssIgnore {
    /**
     * 指定由业务单独清洗的顶层 JSON 字符串字段，留空时沿用整接口豁免。
     *
     * @return 单独处理的字段名
     */
    String[] bodyFields() default {};
}
