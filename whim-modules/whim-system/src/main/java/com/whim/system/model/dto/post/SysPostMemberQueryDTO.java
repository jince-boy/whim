package com.whim.system.model.dto.post;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位成员及候选用户分页条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostMemberQueryDTO extends PageQueryDTO {
    /** 岗位ID。 */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;
    /** true查询已分配成员，false查询未分配的正常用户。 */
    @NotNull(message = "成员查询类型不能为空")
    private Boolean assigned = true;
    /** 用户名，包含匹配。 */
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;
    /** 姓名，包含匹配。 */
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;
    /** 用户主部门ID。 */
    private Long deptId;
    /** 已分配成员的状态筛选；候选用户固定为正常状态。 */
    @Min(value = 0, message = "用户状态无效")
    @Max(value = 1, message = "用户状态无效")
    private Integer status;
}
