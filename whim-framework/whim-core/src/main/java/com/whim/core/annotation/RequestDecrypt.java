package com.whim.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 强制解密接口的 JSON 请求体，响应保持原有格式；支持 POST、PUT、PATCH、DELETE。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestDecrypt {
}
