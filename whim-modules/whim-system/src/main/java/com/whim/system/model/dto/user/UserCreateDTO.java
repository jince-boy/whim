package com.whim.system.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 创建系统账号及主部门，角色和岗位通过独立授权接口分配。
 */
@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须为3至32个字符")
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._@-]*", message = "用户名格式不正确")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须为8至72个字符")
    private String password;
    @NotNull(message = "主部门不能为空")
    @Positive(message = "部门ID必须为正数")
    private Long deptId;
    @NotBlank(message = "姓名不能为空")
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过64个字符")
    private String email;
    @Size(max = 20, message = "电话不能超过20个字符")
    private String phone;
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别只能为0至2")
    @Max(value = 2, message = "性别只能为0至2")
    private Integer gender = 0;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
