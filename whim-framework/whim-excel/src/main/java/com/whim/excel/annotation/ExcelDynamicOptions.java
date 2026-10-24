package com.whim.excel.annotation;

import com.whim.excel.core.ExcelOptionsProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 使用 Spring Bean 提供动态下拉选项。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDynamicOptions {
    /** 动态选项提供者的 Bean 类型。 */
    Class<? extends ExcelOptionsProvider> providerClass();
}
