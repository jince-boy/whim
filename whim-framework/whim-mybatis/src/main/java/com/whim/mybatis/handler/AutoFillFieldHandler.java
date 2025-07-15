package com.whim.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.whim.mybatis.core.model.entity.BaseEntity;
import com.whim.satoken.core.logic.StpAuthManager;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author jince
 * date: 2025/6/17 19:31
 * description: 自动填充字段处理器
 */
public class AutoFillFieldHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            if (Objects.isNull(baseEntity.getCreateTime())) {
                baseEntity.setCreateTime(LocalDateTime.now());
                baseEntity.setUpdateTime(LocalDateTime.now());
            }
            //创建人和修改人的id，默认只有系统用户添加的数据会有，如果是其他用户类型，请在下方自行添加
            if (StpAuthManager.SYSTEM.isLogin()) {
                baseEntity.setCreateBy(StpAuthManager.SYSTEM.getLoginIdAsLong());
                baseEntity.setUpdateBy(StpAuthManager.SYSTEM.getLoginIdAsLong());
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            if (Objects.isNull(baseEntity.getUpdateTime())) {
                baseEntity.setUpdateTime(LocalDateTime.now());
            }
            if (StpAuthManager.SYSTEM.isLogin()) {
                baseEntity.setUpdateBy(StpAuthManager.SYSTEM.getLoginIdAsLong());
            }
        }
    }
}
