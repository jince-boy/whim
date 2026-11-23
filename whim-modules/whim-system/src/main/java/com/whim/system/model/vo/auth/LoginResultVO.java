package com.whim.system.model.vo.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.whim.core.auth.model.AuthenticationToken;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 登录令牌、待验证挑战或恢复完成状态。
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoMapper(target = AuthenticationToken.class, convertGenerate = false)
public class LoginResultVO extends AuthenticationToken {
    /** 仅在密码已验证、仍需第二因素时返回。 */
    private String challengeId;
    /** 仅在恢复码解除旧绑定后返回，客户端需重新登录。 */
    private Boolean recovered;
}
