package com.whim.system.model.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位成员增量配置参数，仅修改明确指定的用户。
 */
@Data
public class SysPostMemberDTO {
    /** 用户ID集合，最多500名。 */
    @NotEmpty(message = "用户ID集合不能为空")
    @Size(max = 500, message = "每次最多配置500名用户")
    private Set<@NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须大于0") Long> userIds;
}
