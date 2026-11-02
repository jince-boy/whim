package com.whim.system.model.dto.dept;

import com.whim.system.model.entity.SysDept;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门修改参数。
 */
@Data
@AutoMapper(target = SysDept.class, reverseConvertGenerate = false)
public class SysDeptUpdateDTO {
    /** 部门ID。 */
    @NotNull(message = "部门ID不能为空")
    private Long id;

    /** 上级部门ID，0表示根部门。 */
    @NotNull(message = "上级部门不能为空")
    @PositiveOrZero(message = "上级部门ID不能小于0")
    private Long parentId;

    /** 部门名称，同一上级下未删除的部门名称唯一。 */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    /** 部门编码，未删除的部门之间唯一。 */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 64, message = "部门编码不能超过64个字符")
    private String deptCode;

    /** 负责人用户ID，null表示清空负责人。 */
    private Long leaderUserId;

    /** 联系电话，null表示清空。 */
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    /** 联系邮箱，null表示清空。 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过64个字符")
    private String email;

    /** 排序值。 */
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能小于0")
    private Integer sort;

    /** 状态：0正常、1停用。 */
    @NotNull(message = "部门状态不能为空")
    @Min(value = 0, message = "部门状态无效")
    @Max(value = 1, message = "部门状态无效")
    private Integer status;

    /** 备注，null表示清空。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
