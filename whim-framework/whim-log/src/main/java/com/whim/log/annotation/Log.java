package com.whim.log.annotation;

import com.whim.log.enums.LogType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 标记需要审计的控制器操作。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 获取操作所属的业务模块。
     *
     * @return 模块名称
     */
    String title();

    /**
     * 获取本次业务操作的类型。
     *
     * @return 操作类型
     */
    LogType type();

    /**
     * 指定本次操作不记录的请求字段，在各层对象和集合元素中按完整字段名区分大小写匹配。
     * 默认为空，涉及密码、密钥等业务字段时由接口显式声明。
     *
     * @return 不写入请求摘要的字段名称
     */
    String[] excludeFields() default {};
}
