package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/13
 * @description 用户头像上传参数，上传途径由服务端固定。
 */
@Getter
@Setter
public class SysUserAvatarUploadDTO {
    /** 本地头像图片。 */
    @NotNull(message = "头像图片不能为空")
    private MultipartFile file;
}
