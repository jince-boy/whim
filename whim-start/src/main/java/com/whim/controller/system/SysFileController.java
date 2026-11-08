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
