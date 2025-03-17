package com.whim.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.common.base.BaseController;
import com.whim.common.utils.FileUtil;
import com.whim.common.web.Result;
import com.whim.file.FileStorageService;
import com.whim.file.model.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @author Jince
 * date: 2024/10/20 20:54
 * description: 测试控制器
 */
@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController extends BaseController {
    private final FileStorageService fileStorageService;

    @GetMapping
    @SaIgnore
    public Result<Boolean> test(@RequestParam("file") MultipartFile file) {
        Boolean upload = fileStorageService.upload(file, builder -> {
            builder.platform("minio").storagePath("ccc");
        });
        log.info(upload.toString());
        return Result.success("上传成功", upload);
    }

    @GetMapping("/file")
    @SaIgnore
    public ResponseEntity<Resource> getFile() {
        FileInfo minio = fileStorageService.getFileInfo(builder -> builder.platform("local").storagePath("ccc").fileName("454ebee6-e998-44c7-b21f-0d46985c0c28.jpg"));
        log.info(minio.toString());
        InputStreamResource inputStreamResource = new InputStreamResource(minio.getInputStream());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(minio.getContentType()));
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(inputStreamResource);
    }

    @GetMapping("/delete")
    @SaIgnore
    public Result<String> deleteFile() {
        if (fileStorageService.deleteFile(builder -> builder.platform("local").storagePath("ccc").fileName("454ebee6-e998-44c7-b21f-0d46985c0c28.jpg"))) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("/test")
    @SaIgnore
    public Result<String> test2() {
        log.info(FileUtil.joinPath2(true, "a"));
        return Result.success("成功");
    }

}