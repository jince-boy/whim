package com.whim.system.model.dto.user;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户完整修改参数，不接收用户名、密码及角色。
 */
@Data
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
public class SysUserUpdateDTO {
    /** 用户ID。 */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 真实姓名。 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;

    /** 主部门ID，null表示未分配部门。 */
    private Long deptId;

    /** 邮箱，null表示清空。 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过64个字符")
    private String email;

    /** 手机号码，null表示清空。 */
    @Size(max = 20, message = "手机号码不能超过20个字符")
    private String phone;

    /** 性别字典编码：0未知、1男、2女。 */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别无效")
    @Max(value = 2, message = "性别无效")
    private Integer gender;

    /** 状态：0启用、1停用。 */
    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态无效")
    @Max(value = 1, message = "用户状态无效")
    private Integer status;

    /** 岗位ID集合，空集合表示不任职任何岗位。 */
    @NotNull(message = "岗位集合不能为空")
    @Size(max = 100, message = "最多选择100个岗位")
    private List<@NotNull(message = "岗位ID不能为空") Long> postIds = new ArrayList<>();

    /** 备注，null表示清空。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
