package com.whim.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.common.base.BaseController;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * date: 2024/10/20 20:54
 * description: 测试控制器
 */
@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class TestController extends BaseController {
    private final FileStorageService fileStorageService;

    @GetMapping("/upload")
    @SaIgnore
    public Result<Boolean> upload(@RequestParam("platform") String platform, @RequestParam("file") MultipartFile file) {
        Boolean upload = fileStorageService.upload(file, builder -> {
            builder.platform(platform).storagePath("ccc");
        });
        log.info(upload.toString());
        return Result.success("上传成功", upload);
    }

    @GetMapping("/getFile")
    @SaIgnore
    public ResponseEntity<Resource> getFile(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        FileInfo minio = fileStorageService.getFileInfo(builder -> builder.platform(platform).storagePath("ccc").fileName(name));
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
        if (fileStorageService.deleteFile(builder -> builder.platform("minio").storagePath("ccc").fileName("237d8071-2d43-472c-86a9-66b965da315a.jpg"))) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @GetMapping("/exists")
    @SaIgnore
    public Result<String> exists(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        if (fileStorageService.exists(builder -> builder.platform(platform).storagePath("ccc").fileName(name))) {
            return Result.success("存在");
        }
        return Result.success("不存在");
    }

    @GetMapping("/list")
    @SaIgnore
    public Result<List<String>> list(@RequestParam("platform") String platform) {
        return Result.success("获取列表成功", fileStorageService.list(builder -> builder.platform(platform).storagePath("ccc")));
    }

    @GetMapping("/getFilePreSignedUrl")
    @SaIgnore
    public Result<String> getFilePreSignedUrl(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        String filePreSignedUrl = fileStorageService.getFilePreSignedUrl(builder -> builder.platform(platform).storagePath("ccc").fileName(name), 60, TimeUnit.SECONDS);
        return Result.success("获取文件预签名地址成功", filePreSignedUrl);
    }

    @GetMapping("/uploadFilePreSignedUrl")
    @SaIgnore
    public Result<String> uploadFilePreSignedUrl(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        String filePreSignedUrl = fileStorageService.uploadFilePreSignedUrl(builder -> builder.platform(platform).storagePath("ccc").fileName(name), 60, TimeUnit.SECONDS);
        return Result.success("获取文件预签名地址成功", filePreSignedUrl);
    }

}