package com.whim.satoken.annotation;

import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.util.SaTokenConsts;
import com.whim.satoken.kit.StpKit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * date: 2025/3/29 13:24
 * description: 系统用户二级认证校验：客户端必须完成二级认证之后，才能进入该方法，否则将被抛出异常。
 */
@SaCheckSafe(type = StpKit.AccountType.SYSTEM)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface SystemCheckSafe {
    /**
     * 要校验的服务
     *
     * @return /
     */
    String value() default SaTokenConsts.DEFAULT_SAFE_AUTH_SERVICE;

}
