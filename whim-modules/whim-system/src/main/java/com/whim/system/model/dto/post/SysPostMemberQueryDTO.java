package com.whim.system.model.dto.post;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位已分配成员和可选用户的分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostMemberQueryDTO extends PageQueryDTO {
    /** true查询已分配用户，false查询尚未分配的正常用户。 */
    @NotNull(message = "成员分配状态不能为空")
    private Boolean assigned = true;

    /** 用户名，模糊匹配。 */
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;

    /** 姓名，模糊匹配。 */
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;

    /** 用户主部门ID。 */
    @Positive(message = "用户主部门ID必须大于0")
    private Long deptId;

    /** 用户状态：0正常、1停用。 */
    @Min(value = 0, message = "用户状态无效")
    @Max(value = 1, message = "用户状态无效")
    private Integer status;
}
