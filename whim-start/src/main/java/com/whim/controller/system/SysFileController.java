package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.file.*;
import com.whim.system.model.vo.file.*;
import com.whim.system.service.ISysFileService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理及存储配置接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/file")
public class SysFileController {
    private final ISysFileService fileService;

    /**
     * 分页查询文件。
     *
     * @param query 查询条件
     * @return 分页查询文件结果
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:file:page")
    public Result<PageDataVO<SysFileVO>> getFilePage(@Valid SysFileQueryDTO query) {
        return Result.success("分页查询文件成功", fileService.getFilePage(query));
    }

    /**
     * 查询存储配置。
     *
     * @return 查询存储配置结果
     */
    @GetMapping("/storageList")
    @SystemCheckPermission("system:file:storageList")
    public Result<List<SysFileStorageVO>> getStorageList() {
        return Result.success("查询存储配置成功", fileService.getStorageList());
    }

    /**
     * 查询上传上限。
     *
     * @return 查询上传上限结果
     */
    @GetMapping("/uploadLimit")
    @SystemCheckPermission("system:file:upload")
    public Result<Long> getUploadLimit() {
        return Result.success("查询上传上限成功", fileService.getUploadLimit());
    }

    /**
     * 获取文件缩略图，沿用下载权限保护文件内容。
     *
     * @param id 文件ID
     * @return PNG 图片响应，不可预览时返回204空响应
     */
    @GetMapping("/thumbnail")
    @SystemCheckPermission("system:file:download")
    public ResponseEntity<byte[]> getFileThumbnail(@RequestParam Long id) {
        byte[] thumbnail = fileService.getFileThumbnail(id);
        if (thumbnail.length == 0) {
            return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        }
        // 仅返回重新编码的 PNG，不将用户上传的原始内容作为内联资源执行。
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentLength(thumbnail.length).body(thumbnail);
    }

    /**
     * 查询本人上传任务中已保存的分片。
     *
     * @param id 上传任务ID
     * @return 上传任务进度
     */
    @GetMapping("/uploadTask")
    @SystemCheckPermission("system:file:upload")
    public Result<SysFileUploadTaskVO> getFileUploadTask(@RequestParam Long id) {
        return Result.success("查询上传进度成功", fileService.getFileUploadTask(id));
    }

    /**
     * 创建可暂停和继续的上传任务。
     *
     * @param parameter 文件信息及幂等请求标识
     * @return 上传任务
     */
    @PostMapping("/uploadTask")
    @SystemCheckPermission("system:file:upload")
    public Result<SysFileUploadTaskVO> addFileUploadTask(@Valid @RequestBody SysFileUploadTaskAddDTO parameter) {
        return Result.success("创建上传任务成功", fileService.addFileUploadTask(parameter));
    }

    /**
     * 上传当前任务的一个分片。
     *
     * @param parameter 任务ID、分片序号及内容
     * @return 上传分片结果
     */
    @PostMapping(value = "/uploadPart", consumes = "multipart/form-data")
    @SystemCheckPermission("system:file:upload")
    public Result<Void> addFileUploadPart(@Valid @ModelAttribute SysFileUploadPartDTO parameter) {
        fileService.addFileUploadPart(parameter);
        return Result.success("上传分片成功");
    }

    /**
     * 合并完整分片并保存文件，重复请求返回相同的文件ID。
     *
     * @param id 上传任务ID
     * @return 已完成的上传任务
     */
    @PostMapping("/completeUpload")
    @SystemCheckPermission("system:file:upload")
    public Result<SysFileUploadTaskVO> completeFileUpload(@RequestParam Long id) {
        return Result.success("文件上传成功", fileService.completeFileUpload(id));
    }

    /**
     * 取消本人未完成的上传任务。
     *
     * @param id 上传任务ID
     * @return 取消结果，已完成任务仍返回完成状态
     */
    @DeleteMapping("/cancelUpload")
    @SystemCheckPermission("system:file:upload")
    public Result<SysFileUploadTaskVO> cancelFileUpload(@RequestParam Long id) {
        return Result.success("上传任务处理成功", fileService.cancelFileUpload(id));
    }

    /**
     * 使用系统默认上传配置上传文件。
     *
     * @param parameter 上传参数
     * @return 上传文件结果
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @SystemCheckPermission("system:file:upload")
    public Result<SysFileVO> addFile(@Valid @ModelAttribute SysFileUploadDTO parameter) {
        return Result.success("上传文件成功", fileService.addFile(parameter));
    }

    /**
     * 新增存储配置。
     *
     * @param parameter 配置参数
     * @return 操作结果
     */
    @PostMapping("/addStorage")
    @SystemCheckPermission("system:file:addStorage")
    public Result<Void> addStorage(@Valid @RequestBody SysFileStorageAddDTO parameter) {
        fileService.addStorage(parameter);
        return Result.success("新增存储配置成功");
    }

    /**
     * 修改存储配置。
     *
     * @param parameter 配置参数
     * @return 操作结果
     */
    @PutMapping("/updateStorage")
    @SystemCheckPermission("system:file:updateStorage")
    public Result<Void> updateStorage(@Valid @RequestBody SysFileStorageUpdateDTO parameter) {
        fileService.updateStorage(parameter);
        return Result.success("修改存储配置成功");
    }

    /**
     * 切换默认存储。
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @PutMapping("/defaultStorage")
    @SystemCheckPermission("system:file:defaultStorage")
    public Result<Void> updateDefaultStorage(@RequestParam Long id) {
        fileService.updateDefaultStorage(id);
        return Result.success("切换默认存储成功");
    }

    /**
     * 删除文件。
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:file:delete")
    public Result<Void> deleteFile(@RequestParam Long id) {
        fileService.deleteFile(id);
        return Result.success("删除文件成功");
    }

    /**
     * 删除存储配置。
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteStorage")
    @SystemCheckPermission("system:file:deleteStorage")
    public Result<Void> deleteStorage(@RequestParam Long id) {
        fileService.deleteStorage(id);
        return Result.success("删除存储配置成功");
    }

    /**
     * 下载文件。
     *
     * @param id 文件ID
     * @param response 下载响应
     */
    @GetMapping("/download")
    @SystemCheckPermission("system:file:download")
    public void downloadFile(@RequestParam Long id, HttpServletResponse response) {
        fileService.downloadFile(id, response);
    }

}
