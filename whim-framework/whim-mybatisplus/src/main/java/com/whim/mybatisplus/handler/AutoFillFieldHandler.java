package com.whim.mybatisplus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.whim.core.auth.AuthenticationContext;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author jince
 * date: 2026/4/9 22:52
 * description: 自动填充字段处理器
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
                baseEntity.setCreateBy(authenticationContext.getCurrentUserInfo().getUserId());
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
            if (Objects.isNull(baseEntity.getUpdateTime())) {
                baseEntity.setUpdateTime(LocalDateTime.now());
            }
            if (authenticationContext.isLogin() && Objects.isNull(baseEntity.getUpdateBy())) {
                baseEntity.setUpdateBy(authenticationContext.getCurrentUserInfo().getUserId());
            }
        }
    }
}
