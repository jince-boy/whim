package com.whim.satoken.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.whim.satoken.constants.AuthUserType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * @date 2026/4/10
 * @description 系统用户权限认证：必须具有指定权限才能进入该方法
 */
@SaCheckPermission(type = AuthUserType.SYSTEM)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SystemCheckPermission {
    /**
     * 需要校验的权限码
     *
     * @return 需要校验的权限码
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    /**
     * 验证模式：AND | OR，默认AND
     *
     * @return 验证模式
     */
    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    /**
     * 权限校验失败时的“备选方案”，
     * 只要主条件（如权限）或 orRole 中任意一个校验通过即可放行。
     * 示例1：
     *   SaCheckPermission(value = "user-add", orRole = "admin")
     *   含义：有 user-add 权限 或者 是 admin 角色，就能通过校验。
     * 示例2：
     *   orRole = {"admin", "manager", "staff"}
     *   含义：三个角色中，拥有任意一个即可通过。
     * 示例3：
     *   orRole = {"admin, manager, staff"}
     *   含义：必须同时拥有 admin、manager、staff 三个角色才能通过。
     *
     * @return 角色标识或角色标识数组
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};

}
