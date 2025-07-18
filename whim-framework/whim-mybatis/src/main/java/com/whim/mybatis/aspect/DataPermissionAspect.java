package com.whim.mybatis.aspect;

import com.whim.mybatis.annotation.DataPermission;
import com.whim.mybatis.context.DataPermissionContext;
import com.whim.satoken.core.context.AuthContext;
import com.whim.satoken.core.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Objects;

/**
 * @author jince
 * @date 2025/7/9 14:10
 * @description 数据权限切面
 */
@Slf4j
@Aspect
public class DataPermissionAspect {
    @Around("@annotation(dataPermission)")
    public Object applyPermission(ProceedingJoinPoint point, DataPermission dataPermission) throws Throwable {
        try {
            DataPermissionContext.DataPermissionHolder dataPermissionHolder = new DataPermissionContext.DataPermissionHolder(dataPermission);
            UserInfo userInfo = AuthContext.getUserInfo();
            if (Objects.nonNull(userInfo)) {
                dataPermissionHolder.addAttribute("userInfo", userInfo);
            }
            DataPermissionContext.pushPermissionHolder(dataPermissionHolder);
            return point.proceed();
        } catch (Throwable e) {
            log.error("数据权限切面异常:{}", e.getMessage(), e);
            throw e;
        } finally {
            DataPermissionContext.popPermissionHolder();
        }
    }
}
