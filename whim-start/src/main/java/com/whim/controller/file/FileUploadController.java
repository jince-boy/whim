package com.whim.controller.file;

import com.whim.file.model.PartUploadSignature;
import com.whim.filemanagement.model.dto.AddUploadTaskDTO;
import com.whim.filemanagement.model.dto.UploadPartDTO;
import com.whim.filemanagement.model.vo.UploadOptionsVO;
import com.whim.filemanagement.model.vo.UploadTaskVO;
import com.whim.filemanagement.service.impl.FileUploadService;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 个人文件上传接口；功能权限与任务归属同时校验，所有 JSON 响应复用统一格式。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/fileUpload")
@SystemCheckLogin
@ConditionalOnProperty(prefix = "file.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadController {
    private final FileUploadService fileUploadService;

    /** 获取生效的上传容量与并发限制。 */
    @GetMapping("/options")
    @SystemCheckPermission("system:fileUpload:list")
    public Result<UploadOptionsVO> getUploadOptions() {
        return Result.success("获取上传配置成功", fileUploadService.getUploadOptions());
    }

    /** 分页列出当前用户的任务及文件。 */
    @GetMapping("/tasks")
    @SystemCheckPermission("system:fileUpload:list")
    public Result<PageDataVO<UploadTaskVO>> getUploadTasks(@Valid PageQueryDTO query) {
        return Result.success("获取上传任务成功", fileUploadService.getUploadTasks(query));
    }

    /** 幂等创建上传任务；同一请求标识必须对应同一文件。 */
    @PostMapping("/tasks")
    @SystemCheckPermission("system:fileUpload:upload")
    public Result<UploadTaskVO> addUploadTask(@RequestBody @Valid AddUploadTaskDTO request) {
        return Result.success("创建上传任务成功", fileUploadService.addUploadTask(request));
    }

    /** 查询存储端确认的分片进度。 */
    @GetMapping("/tasks/{taskId}")
    @SystemCheckPermission("system:fileUpload:list")
    public Result<UploadTaskVO> getUploadTask(@PathVariable String taskId) {
        return Result.success("获取上传进度成功", fileUploadService.getUploadTask(taskId));
    }

    /** 恢复已登记但初始化失败的任务，不延长原有效期。 */
    @PostMapping("/tasks/{taskId}/resume")
    @SystemCheckPermission("system:fileUpload:upload")
    public Result<UploadTaskVO> resumeUpload(@PathVariable String taskId) {
        return Result.success("恢复上传任务成功", fileUploadService.resumeUpload(taskId));
    }

    /** 登记不可变分片摘要；云存储返回签名，本地模式返回空 URL。 */
    @PostMapping("/tasks/{taskId}/parts/{partNumber}/signature")
    @SystemCheckPermission("system:fileUpload:upload")
    public Result<PartUploadSignature> getPartUploadSignature(@PathVariable String taskId,
            @PathVariable int partNumber, @RequestBody @Valid UploadPartDTO request) {
        return Result.success("登记分片成功",
                fileUploadService.getPartUploadSignature(taskId, partNumber,
                        request.getContentMd5(), request.getContentSha256()));
    }

    /** 本地分片上传；MultipartFile 受 Servlet 容量限制，服务层进一步核对精确长度。 */
    @PutMapping(value = "/tasks/{taskId}/parts/{partNumber}", consumes = "multipart/form-data")
    @SystemCheckPermission("system:fileUpload:upload")
    public Result<Void> uploadPart(@PathVariable String taskId, @PathVariable int partNumber,
                                  @RequestPart("file") MultipartFile file) throws IOException {
        try (var input = file.getInputStream()) {
            fileUploadService.uploadPart(taskId, partNumber, input, file.getSize());
        }
        return Result.success("分片上传成功");
    }

    /** 服务端核对分片并完成上传，可重试确认已成功的合并。 */
    @PostMapping("/tasks/{taskId}/complete")
    @SystemCheckPermission("system:fileUpload:upload")
    public Result<UploadTaskVO> completeUpload(@PathVariable String taskId) {
        return Result.success("文件上传完成", fileUploadService.completeUpload(taskId));
    }

    /** 幂等取消未合并任务，清理失败由恢复调度重试。 */
    @DeleteMapping("/tasks/{taskId}")
    @SystemCheckPermission("system:fileUpload:cancel")
    public Result<Void> cancelUpload(@PathVariable String taskId) {
        fileUploadService.cancelUpload(taskId);
        return Result.success("上传任务已取消");
    }

    /** 流式下载本人已完成的文件，强制附件与 nosniff，避免把上传内容当页面执行。 */
    @GetMapping("/tasks/{taskId}/download")
    @SystemCheckPermission("system:fileUpload:download")
    public void downloadFile(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        UploadTaskVO task = fileUploadService.getUploadTask(taskId);
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new IllegalArgumentException("文件尚未完成上传");
        }
        try (var download = fileUploadService.getFileDownloadHandler(taskId)) {
            response.setContentType("application/octet-stream");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                    .filename(task.getFileName(), StandardCharsets.UTF_8).build().toString());
            response.setContentLengthLong(task.getFileSize());
            download.writeToOutputStream(response.getOutputStream());
        } catch (IOException | RuntimeException exception) {
            // 未发送字节时移除二进制响应头，使统一异常处理仍可返回完整 JSON。
            if (!response.isCommitted()) {
                response.reset();
            }
            throw exception;
        }
    }
}

