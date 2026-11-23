package com.whim.system.model.vo.auth;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 待确认的绑定资料，仅在校验本人密码后返回。
 */
@Getter
@Setter
public class TwoFactorSetupVO {
    /** 本次绑定凭证。 */
    private String setupId;
    /** 扫码或手工录入验证器使用的 Base32 密钥。 */
    private String secret;
    /** 丢失验证器时使用的一次性恢复码。 */
    private String recoveryCode;
}
