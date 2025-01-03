package com.whim.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.common.web.Result;
import com.whim.file.FileInfo;
import com.whim.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * date: 2024/10/20 20:54
 * description: 测试控制器
 */
@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {
    private final FileStorageService fileStorageService;

    @GetMapping
    @SaIgnore
    public Result<FileInfo> test(@RequestParam("file") MultipartFile file) {
        fileStorageService.wrap(file)
                .createFileHandler(fileConfig -> {
                    fileConfig.setPath("/123");
                });

        fileStorageService.createFileHandler(file -> {
                    file.setPath("/").setFileName("123").setPlatform("local")
                }).
                createImageHandler(img -> {
                    img.size(100, 200);
                })
                .upload();
    }

}
//FileInfo hello = fileStorageService.createFileHandler(file)
//        .image(img -> img.width(200).height(200))
//        .setFileName("hello")
//        .setPath("/test/aaa")
//        .setPlatform("local")
//        .upload();
//        return Result.success("上传成功", hello);
