package com.whim.web.annotation;

import com.whim.web.enums.DesensitizationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 字符串字段脱敏注解，支持基于角色或权限动态决定是否展示原始值。
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Desensitize {

    /**
     * 指定脱敏类型。
     *
     * @return 脱敏类型
     */
    DesensitizationType type() default DesensitizationType.CUSTOM;

    /**
     * 指定允许查看原始值的角色集合。
     *
     * @return 角色集合
     */
    String[] roles() default {};

    /**
     * 指定允许查看原始值的权限集合。
     *
     * @return 权限集合
     */
    String[] authorities() default {};

    /**
     * 指定角色与权限匹配是否必须全部满足。
     *
     * @return 为 true 表示必须全部满足
     */
    boolean requireAll() default false;

    /**
     * 自定义脱敏时前缀保留长度。
     *
     * @return 前缀保留长度
     */
    int prefixKeep() default 0;

    /**
     * 自定义脱敏时后缀保留长度。
     *
     * @return 后缀保留长度
     */
    int suffixKeep() default 0;

    /**
     * 指定脱敏字符。
     *
     * @return 脱敏字符
     */
    char maskChar() default '*';
}
