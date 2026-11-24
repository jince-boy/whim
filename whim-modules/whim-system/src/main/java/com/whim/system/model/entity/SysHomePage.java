package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页关联配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysHomePage extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer targetType;
    private Long targetId;
    private String path;
    private String component;
    /** 绑定已有菜单时保留其身份，删除或换址后不能自动变成独立授权。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long menuId;
    private Integer priority;
    private Integer status;
    @TableLogic
    private Long deleted;
}
