package com.whim.system.service;

import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jince
 * @date 2026/11/13
 * @description 用户头像上传及安全图片读取。
 */
public interface ISysUserAvatarService {
    /**
     * 读取头像来源文件的安全缩略图。
     *
     * @param id 头像文件ID
     * @return PNG缩略图，不存在或不可预览时为空数组
     */
    byte[] getUserAvatar(Long id);

    /**
     * 校验本地图片并通过默认存储上传头像。
     *
     * @param parameter 头像上传参数
     * @return 用于保存用户资料的头像地址
     */
    String addUserAvatar(SysUserAvatarUploadDTO parameter);

    /**
     * 将头像原文件写入认证响应，保留上传内容及原始尺寸。
     *
     * @param id 头像文件ID
     * @param response 原图下载响应
     */
    void downloadUserAvatar(Long id, HttpServletResponse response);
}
