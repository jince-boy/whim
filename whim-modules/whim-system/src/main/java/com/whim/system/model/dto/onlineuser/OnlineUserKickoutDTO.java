package com.whim.system.model.dto.onlineuser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 单会话强退目标。
 */
@Data
public class OnlineUserKickoutDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "会话标识不能为空")
    @Pattern(regexp = "[0-9a-f]{64}", message = "会话标识格式错误")
    private String sessionId;
}
