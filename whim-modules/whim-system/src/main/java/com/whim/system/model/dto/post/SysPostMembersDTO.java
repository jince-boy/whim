package com.whim.system.model.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位成员增量分配或解除参数。
 */
@Data
public class SysPostMembersDTO {
    /** 岗位ID。 */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;
    /** 本次明确选择的用户ID，不替换其他成员。 */
    @NotEmpty(message = "用户ID不能为空")
    @Size(max = 500, message = "每次最多处理500名用户")
    private Set<@NotNull(message = "用户ID不能为null") Long> userIds;
}
