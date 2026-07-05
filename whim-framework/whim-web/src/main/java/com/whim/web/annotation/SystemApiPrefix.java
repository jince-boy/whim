package com.whim.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 系统模块接口路径前缀注解，标注在控制器类上，自动为所有接口添加 /system 前缀
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiPrefix("/system")
public @interface SystemApiPrefix {
}
