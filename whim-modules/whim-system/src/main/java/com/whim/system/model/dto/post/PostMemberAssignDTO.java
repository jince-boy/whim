package com.whim.system.model.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 岗位成员覆盖式分配参数。
 */
@Data
public class PostMemberAssignDTO {
    @NotNull(message = "成员ID集合不能为空")
    @Size(max = 200, message = "一次最多分配200名成员")
    private Set<@NotNull(message = "成员ID不能为空") Long> userIds;
}
