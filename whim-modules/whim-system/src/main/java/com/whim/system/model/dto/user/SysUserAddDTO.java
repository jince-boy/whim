package com.whim.system.model.dto.user;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户新增参数，角色由独立授权流程维护。
 */
@Data
@AutoMapper(target = SysUser.class, reverseConvertGenerate = false)
public class SysUserAddDTO {
    /** 用户名，统一小写且删除后不可复用。 */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "[A-Za-z0-9_]{3,32}", message = "用户名须为3~32位字母、数字或下划线")
    private String username;

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
    private Integer gender = 0;

    /** 状态：0启用、1停用。 */
    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态无效")
    @Max(value = 1, message = "用户状态无效")
    private Integer status = 0;

    /** 岗位ID集合，空集合表示不任职任何岗位。 */
    @NotNull(message = "岗位集合不能为空")
    @Size(max = 100, message = "最多选择100个岗位")
    private List<@NotNull(message = "岗位ID不能为空") Long> postIds = new ArrayList<>();

    /** 备注，null表示清空。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;

    /** 密码保留原输入，不允许包含空白字符。 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须在8~72位之间")
    @Pattern(regexp = "(?U)\\S*", message = "密码不能包含空白字符")
    @JsonDeserialize(using = StringDeserializer.class)
    @ToString.Exclude
    private String password;

    /**
     * 校验 BCrypt 支持的密码字节长度。
     *
     * @return 密码的UTF-8编码是否不超过72字节
     */
    @AssertTrue(message = "密码的UTF-8编码不能超过72字节")
    public boolean isPasswordLengthValid() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
