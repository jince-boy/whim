package com.whim.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.common.web.Result;
import com.whim.file.FileStorageService;
import com.whim.file.model.vo.FileInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public Result<FileInfoVO> test(@RequestParam("file") MultipartFile file) {
        return Result.success("上传成功", fileStorageService.upload(file, builder -> builder.platform("local").storagePath("aaa")));
    }

    @GetMapping("/file")
    @SaIgnore
    public ResponseEntity<Resource> getFile() throws IOException {
        Tika tika = new Tika();
        Resource resource = new InputStreamResource(fileStorageService.getFile(builder -> builder.platform("minio").storagePath("/aaa").fileName("微信图片_20250305165025.jpg")));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(tika.detect(fileStorageService.getFile(builder -> builder.platform("minio").storagePath("/aaa").fileName("微信图片_20250305165025.jpg")))));
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(resource);
    }

}