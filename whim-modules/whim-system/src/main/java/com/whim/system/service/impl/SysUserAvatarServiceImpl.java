package com.whim.system.service.impl;

import com.whim.core.exception.ServiceException;
import com.whim.core.constant.CacheKeys;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.model.dto.file.SysFileUploadDTO;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.entity.SysFile;
import com.whim.system.service.ISysFileService;
import com.whim.system.service.ISysUserAvatarService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/13
 * @description 复用文件存储与图片解码能力维护用户头像。
 */
@Service
@RequiredArgsConstructor
public class SysUserAvatarServiceImpl implements ISysUserAvatarService {
    private static final String UPLOAD_SOURCE = "userAvatar";
    private static final long MAXIMUM_AVATAR_BYTES = 5 * 1024 * 1024;
    private static final Set<String> AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private final ISysFileService fileService;

    /**
     * 读取头像来源文件的安全缩略图。
     *
     * @param id 头像文件ID
     * @return PNG缩略图，不存在或不可预览时为空数组
     */
    @Override
    public byte[] getUserAvatar(Long id) {
        // 登录用户可展示头像，但不能借此读取文件管理中的其他附件。
        if (!fileService.lambdaQuery().eq(SysFile::getId, id)
                .eq(SysFile::getUploadSource, UPLOAD_SOURCE).exists()) {
            return new byte[0];
        }
        return fileService.getFileThumbnail(id);
    }

    /**
     * 校验本地图片并通过默认存储上传头像。
     *
     * @param parameter 头像上传参数
     * @return 用于保存用户资料的头像地址
     */
    @Override
    public String addUserAvatar(SysUserAvatarUploadDTO parameter) {
        MultipartFile file = parameter.getFile();
        if (file.isEmpty() || file.getSize() > MAXIMUM_AVATAR_BYTES) {
            throw new ServiceException("头像图片不能为空且不能超过5MB");
        }
        if (!AVATAR_CONTENT_TYPES.contains(Objects.requireNonNullElse(file.getContentType(), ""))) {
            throw new ServiceException("头像仅支持JPG、PNG或GIF图片");
        }
        // 实际解码成功才接收头像，不能仅凭扩展名或客户端声明判断图片有效。
        try {
            if (ImageThumbnailGenerator.generateThumbnail(file.getInputStream(), 96, 96).length == 0) {
                throw new ServiceException("头像图片无法识别、已损坏或像素过大，请更换图片");
            }
        } catch (IOException exception) {
            throw new ServiceException("读取头像图片失败，请重新选择图片");
        }
        SysFileUploadDTO upload = new SysFileUploadDTO();
        upload.setFile(file);
        upload.setUploadSource(UPLOAD_SOURCE);
        // 用户资料保存前只生成文件，取消或保存失败不会改变原头像。
        return "/system/user/avatar?id=" + fileService.addFile(upload).getId();
    }

    /**
     * 将头像原文件写入认证响应，保留上传内容及原始尺寸。
     *
     * @param id 头像文件ID
     * @param response 原图下载响应
     */
    @Override
    public void downloadUserAvatar(Long id, HttpServletResponse response) {
        // 头像原图入口不授予普通附件下载能力，仍只接受头像来源及头像上传限制内的图片。
        if (!fileService.lambdaQuery().eq(SysFile::getId, id)
                .eq(SysFile::getUploadSource, UPLOAD_SOURCE)
                .in(SysFile::getContentType, AVATAR_CONTENT_TYPES)
                .le(SysFile::getFileSize, MAXIMUM_AVATAR_BYTES).exists()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            return;
        }
        // 复用原文件流式下载及存储读锁；以附件返回，前端取得Blob后在图片元素中展示。
        fileService.downloadFile(id, response);
    }

    /**
     * 应用迁移完成后失效上传途径字典缓存，使新增头像来源立即可用。
     */
    @EventListener(ApplicationStartedEvent.class)
    public void refreshUserAvatarUploadSourceCache() {
        // Flyway更新了已有字典，Redis中上次运行的旧选项不能阻止首次头像上传。
        CacheUtils.evict(CacheKeys.SYS_DICT, "sys_file_upload_source");
    }
}
