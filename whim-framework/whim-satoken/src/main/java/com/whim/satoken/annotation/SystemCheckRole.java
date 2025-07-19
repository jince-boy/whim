package com.whim.satoken.annotation;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.whim.satoken.core.common.AccountType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * @date 2025/3/29 13:23
 * @description 系统用户角色认证：必须具有指定角色标识才能进入该方法
 */
@SaCheckRole(type = AccountType.SYSTEM)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface SystemCheckRole {
    /**
     * 需要校验的角色标识
     * @return 需要校验的角色标识
     */
    @AliasFor(annotation = SaCheckRole.class)
    String [] value() default {};

    /**
     * 验证模式：AND | OR，默认AND
     * @return 验证模式
     */
    @AliasFor(annotation = SaCheckRole.class)
    SaMode mode() default SaMode.AND;
}
