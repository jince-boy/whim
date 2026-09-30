package com.whim.mybatisplus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 标记框架授权所需的内部只读元数据查询，不能用于业务数据接口或写入。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermissionMetadata {
}
