package com.whim.system.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户基础信息修改，账号名、密码、部门及授权通过独立操作管理。
 */
@Data
public class UserUpdateDTO {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;
    @Size(max = 255, message = "头像地址不能超过255个字符")
    private String avatar;
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过64个字符")
    private String email;
    @Size(max = 20, message = "电话不能超过20个字符")
    private String phone;
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别只能为0至2")
    @Max(value = 2, message = "性别只能为0至2")
    private Integer gender;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
