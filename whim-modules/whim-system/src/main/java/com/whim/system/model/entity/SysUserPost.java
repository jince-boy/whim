package com.whim.system.model.entity;

import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 用户岗位关联实体，解除关联时物理删除。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPost extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 关联ID。 */
    private Long id;
    /** 用户ID。 */
    private Long userId;
    /** 岗位ID。 */
    private Long postId;
}
