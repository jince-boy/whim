package com.whim.satoken.annotation;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.whim.satoken.kit.StpKit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * date: 2025/3/29 13:21
 * description: 系统用户登录认证：只有登录之后才能进入该方法
 */
@SaCheckLogin(type = StpKit.AccountType.SYSTEM)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SystemCheckLogin {
}
