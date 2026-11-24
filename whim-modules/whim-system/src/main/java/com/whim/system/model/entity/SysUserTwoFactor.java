package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 用户自行绑定的两步验证凭据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserTwoFactor extends BaseEntity {
    private Long id;
    private Long userId;
    @ToString.Exclude
    private String encryptedSecret;
    private Long lastStep;
    @ToString.Exclude
    private String recoveryHash;
    @TableLogic
    private Long deleted;
}
