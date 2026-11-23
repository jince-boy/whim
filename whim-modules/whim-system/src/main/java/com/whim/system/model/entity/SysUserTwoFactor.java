package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 用户自行绑定的两步验证凭据。
 */
@Getter
@Setter
public class SysUserTwoFactor extends BaseEntity {
    private Long id;
    private Long userId;
    private String encryptedSecret;
    private Long lastStep;
    private String recoveryHash;
    @TableLogic
    private Long deleted;
}
