package com.whim.controller.test;

import cn.dev33.satoken.annotation.SaIgnore;
import com.whim.common.base.BaseController;
import com.whim.common.web.Result;
import com.whim.file.FileStorageService;
import com.whim.file.model.MetaData;
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
import java.io.InputStream;
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
    private final Tika tika;

    @GetMapping("/upload")
    @SaIgnore
    public Result<MetaData> upload(@RequestParam("platform") String platform, @RequestParam("file") MultipartFile file) {
        MetaData metaData = fileStorageService.upload(file, builder -> builder.platform(platform).storagePath("ccc").fileName("123.jpg"));
        return Result.success("上传成功", metaData);
    }

    @GetMapping("/getFile")
    @SaIgnore
    public ResponseEntity<Resource> getFile(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        InputStream inputStream = fileStorageService.download(builder -> builder.platform(platform).storagePath("ccc").fileName(name)).getInputStream();
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        String detect = null;
        try {
            detect = tika.detect(inputStream);
            inputStream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(detect));
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(inputStreamResource);
    }

    @GetMapping("/delete")
    @SaIgnore
    public Result<String> deleteFile(@RequestParam("platform") String platform, @RequestParam("path") String path, @RequestParam("name") String name) {
        if (fileStorageService.deleteFile(builder -> builder.platform(platform).storagePath(path).fileName(name))) {
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

    @GetMapping("/getFilePreSignedUrl")
    @SaIgnore
    public Result<String> getFilePreSignedUrl(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        String filePreSignedUrl = fileStorageService.getFilePreSignedUrl(builder -> builder.platform(platform).storagePath("ccc").fileName(name), 60, TimeUnit.SECONDS);
        return Result.success("获取文件预签名地址成功", filePreSignedUrl);
    }

    @GetMapping("/uploadFilePreSignedUrl")
    @SaIgnore
    public Result<String> uploadFilePreSignedUrl(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        String filePreSignedUrl = fileStorageService.uploadFilePreSignedUrl(builder -> builder.platform(platform).storagePath("ccc").fileName(name).contentType("image/jpeg"), 3, TimeUnit.HOURS);
        return Result.success("获取文件预签名地址成功", filePreSignedUrl);
    }

    @GetMapping("/getFileMetaData")
    @SaIgnore
    public Result<MetaData> getFileMetaData(@RequestParam("platform") String platform, @RequestParam("name") String name) {
        MetaData fileMetaData = fileStorageService.getFileMetaData(builder -> builder.platform(platform).storagePath("ccc").fileName(name));
        return Result.success("获取文件元数据成功", fileMetaData);
    }

}