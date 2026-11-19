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
     * 指定可以保存的请求字段，仅支持直接参数或请求对象的顶层属性。
     *
     * @return 可记录的业务字段名称
     */
    String[] requestFields() default {};
}
