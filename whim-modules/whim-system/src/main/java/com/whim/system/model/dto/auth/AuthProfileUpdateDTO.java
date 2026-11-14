package com.whim.system.model.dto.auth;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 当前登录用户可自行维护的个人资料，不接收用户ID或组织授权字段。
 */
@Data
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
public class AuthProfileUpdateDTO {
    /** 真实姓名。 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;

    /** 上传返回的头像地址，null表示移除头像，兼容纯文本清洗后的等号。 */
    @Size(max = 255, message = "头像地址不能超过255个字符")
    @Pattern(regexp = "/system/user/avatar\\?id(?:=|&#61;)[0-9]+", message = "请使用上传头像接口返回的头像地址")
    private String avatar;

    /** 手机号码，null表示清空。 */
    @Size(max = 20, message = "手机号码不能超过20个字符")
    private String phone;

    /** 联系邮箱，null表示清空。 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过64个字符")
    private String email;

    /** 性别字典编码。 */
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别无效")
    @Max(value = 2, message = "性别无效")
    private Integer gender;
}
