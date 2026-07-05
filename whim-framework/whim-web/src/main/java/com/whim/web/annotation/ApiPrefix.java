package com.whim.web.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/07/05
 * @description 接口路径前缀元注解，用于定义业务模块专属的接口前缀。
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPrefix {

    /**
     * 接口路径前缀。
     *
     * @return 接口路径前缀
     */
    @AliasFor("path")
    String value() default "";

    /**
     * 接口路径前缀。
     *
     * @return 接口路径前缀
     */
    @AliasFor("value")
    String path() default "";
}
