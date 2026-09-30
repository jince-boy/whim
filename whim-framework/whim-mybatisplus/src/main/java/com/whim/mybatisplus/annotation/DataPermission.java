package com.whim.mybatisplus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 在公开业务服务方法声明操作权限及参与过滤的业务表。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    /** 本次操作的精确权限码。 */
    String permission();

    /** 开发者定义的表与业务归属列，支持多表及表别名。 */
    DataPermissionTable[] tables();
}
