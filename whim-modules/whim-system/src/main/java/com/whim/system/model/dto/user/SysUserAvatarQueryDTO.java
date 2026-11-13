package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/13
 * @description 用户头像缩略图及原图查询参数。
 */
@Data
public class SysUserAvatarQueryDTO {
    /** 头像文件ID。 */
    @NotNull(message = "头像文件ID不能为空")
    private Long id;

    /** true读取已保存的头像原文件，默认读取96像素缩略图。 */
    private Boolean preview = false;
}
