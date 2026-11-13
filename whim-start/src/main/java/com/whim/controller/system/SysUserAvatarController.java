package com.whim.controller.system;

import cn.dev33.satoken.annotation.SaMode;
import com.whim.file.support.FileUploadSecurity;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.dto.user.SysUserAvatarQueryDTO;
import com.whim.system.service.ISysUserAvatarService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Jince
 * @date 2026/11/13
 * @description 用户管理头像上传、缩略图展示及原图读取接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/user")
public class SysUserAvatarController {
    private final ISysUserAvatarService userAvatarService;

    /**
     * 为已登录用户返回头像缩略图或未经缩放的头像原文件。
     *
     * @param query 头像文件及预览方式
     * @param response 图片响应，原文件以附件返回
     * @throws IOException 写入缩略图响应失败
     */
    @GetMapping("/avatar")
    public void getUserAvatar(@Valid @ModelAttribute SysUserAvatarQueryDTO query,
                              HttpServletResponse response) throws IOException {
        // 点击预览时读取存储原文件，不能用放大的缩略图代替原图。
        if (Boolean.TRUE.equals(query.getPreview())) {
            userAvatarService.downloadUserAvatar(query.getId(), response);
            return;
        }
        byte[] avatar = userAvatarService.getUserAvatar(query.getId());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (avatar.length == 0) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", FileUploadSecurity.CONTENT_SECURITY_POLICY);
        response.setContentLength(avatar.length);
        response.getOutputStream().write(avatar);
    }

    /**
     * 上传头像并返回地址，由新增或修改用户请求保存关联。
     *
     * @param parameter 本地头像图片
     * @return 头像地址
     */
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SystemCheckPermission(value = {"system:user:create", "system:user:update"}, mode = SaMode.OR)
    public Result<String> addUserAvatar(@Valid @ModelAttribute SysUserAvatarUploadDTO parameter) {
        return Result.success("头像上传成功", userAvatarService.addUserAvatar(parameter));
    }
}
