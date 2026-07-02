package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 用户岗位关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPost extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -22619908774593753L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;

}

