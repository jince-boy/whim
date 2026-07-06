package com.whim.mybatisplus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.whim.core.auth.AuthenticationContext;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/09
 * @description 自动填充字段处理器
 */
@RequiredArgsConstructor
public class AutoFillFieldHandler implements MetaObjectHandler {
    private final AuthenticationContext authenticationContext;

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            if (Objects.isNull(baseEntity.getCreateTime())) {
                baseEntity.setCreateTime(LocalDateTime.now());
            }
            if (authenticationContext.isLogin() && Objects.isNull(baseEntity.getCreateBy())) {
                baseEntity.setCreateBy(authenticationContext.getUserId());
            }
        }
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            LocalDateTime now = LocalDateTime.now();
            boolean login = authenticationContext.isLogin();
            Long userId = login ? authenticationContext.getUserId() : null;
            Object deleted = getFieldValByName("deleted", metaObject);

            if (Objects.equals(deleted, 1)) {
                baseEntity.setDeleteTime(now);
                if (login) {
                    baseEntity.setDeleteBy(userId);
                }
                return;
            }
            if (Objects.isNull(baseEntity.getUpdateTime())) {
                baseEntity.setUpdateTime(now);
            }
            if (login && Objects.isNull(baseEntity.getUpdateBy())) {
                baseEntity.setUpdateBy(userId);
            }
        }
    }
}
