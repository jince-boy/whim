package com.whim.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.file.FileStorageService;
import com.whim.file.model.MetaData;
import com.whim.satoken.kit.StpKit;
import com.whim.system.model.entity.SysUser;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jince
 * date: 2025/6/18 10:43
 * description:
 */
@SystemApiPrefix
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final FileStorageService fileStorageService;
    private final ISysUserService userService;

    @GetMapping
    public Result<SysUser> getUser() {
        log.info(StpKit.SYSTEM.getSession().getId());
        log.info(StpKit.SYSTEM.getTokenSession().get("loginUserInfo").toString());
        return Result.success("success", userService.getSysUserByUsername("admin"));
    }

    @GetMapping("/upload")
    @SaIgnore
    public Result<MetaData> upload(@RequestParam("platform") String platform, @RequestParam("file") MultipartFile file) {
        MetaData metaData = fileStorageService.upload(file, builder -> builder.platform(platform).storagePath("ccc").fileName("123.jpg"));
        return Result.success("上传成功", metaData);
    }
}
