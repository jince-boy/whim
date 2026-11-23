package com.whim.controller.system;

import cn.dev33.satoken.annotation.SaMode;
import com.whim.file.support.FileUploadSecurity;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.config.SysConfigAddDTO;
import com.whim.system.model.dto.config.SysConfigQueryDTO;
import com.whim.system.model.dto.config.SysConfigUpdateDTO;
import com.whim.system.model.vo.config.SiteSettingsVO;
import com.whim.system.model.vo.config.SysConfigVO;
import com.whim.system.service.ISysConfigService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 业务配置管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/config")
public class SysConfigController {
    private final ISysConfigService configService;

    /**
     * 获取登录前即可展示的网站配置。
     *
     * @return 网站标题、介绍、版权和当前 Logo 文件标识
     */
    @GetMapping("/site")
    public Result<SiteSettingsVO> getSiteSettings() {
        return Result.success("网站信息查询成功", configService.getSiteSettings());
    }

    /**
     * 获取当前公开系统 Logo 的 PNG 缩略图。
     *
     * @param id 网站配置返回的 Logo 文件ID
     * @return 图片或空响应
     */
    @GetMapping("/siteLogo")
    public ResponseEntity<byte[]> getSiteLogo(@RequestParam Long id) {
        return getImageResponse(configService.getSiteLogo(id));
    }

    /**
     * 获取指定配置记录的图片缩略图。
     *
     * @param id 配置记录ID
     * @return 图片或空响应
     */
    @GetMapping("/image")
    @SystemCheckPermission("system:config:page")
    public ResponseEntity<byte[]> getConfigImage(@RequestParam Long id) {
        return getImageResponse(configService.getConfigImage(id));
    }

    /**
     * 上传供新增或修改配置使用的图片。
     *
     * @param file 本地图片
     * @return 配置图片文件ID
     */
    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SystemCheckPermission(value = {"system:config:create", "system:config:update"}, mode = SaMode.OR)
    @Log(title = "上传配置图片", type = LogType.INSERT, excludeFields = "file")
    public Result<String> addConfigImage(@RequestParam MultipartFile file) {
        return Result.success("配置图片上传成功", configService.addConfigImage(file).toString());
    }

    /**
     * 分页查询指定分类的配置。
     *
     * @param query 分类、筛选条件和分页参数
     * @return 配置分页数据
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:config:page")
    public Result<PageDataVO<SysConfigVO>> getConfigPage(@Valid SysConfigQueryDTO query) {
        return Result.success("配置查询成功", configService.getConfigPage(query));
    }

    /**
     * 新增自定义配置。
     *
     * @param parameter 配置名称、键、值类型、选项、配置值与备注
     * @return 操作结果
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:config:create")
    @Log(title = "系统配置", type = LogType.INSERT, excludeFields = "configValue")
    public Result<Void> addConfig(@Valid @RequestBody SysConfigAddDTO parameter) {
        configService.addConfig(parameter);
        return Result.success("配置新增成功");
    }

    /**
     * 修改配置值，自定义配置还可调整名称、类型、选项和备注。
     *
     * @param parameter 配置ID与可修改字段
     * @return 操作结果
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:config:update")
    @Log(title = "系统配置", type = LogType.UPDATE, excludeFields = "configValue")
    public Result<Void> updateConfig(@Valid @RequestBody SysConfigUpdateDTO parameter) {
        configService.updateConfig(parameter);
        return Result.success("配置修改成功");
    }

    /**
     * 按主键删除自定义配置。
     *
     * @param id 配置主键
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:config:delete")
    @Log(title = "系统配置", type = LogType.DELETE)
    public Result<Void> deleteConfigById(@RequestParam Long id) {
        configService.deleteConfigById(id);
        return Result.success("配置删除成功");
    }

    /**
     * 输出文件模块重新编码的 PNG 图片。
     *
     * @param image 图片内容
     * @return 图片响应，未配置或无法预览时返回空响应
     */
    private ResponseEntity<byte[]> getImageResponse(byte[] image) {
        if (image.length == 0) {
            return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG)
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", FileUploadSecurity.CONTENT_SECURITY_POLICY)
                .contentLength(image.length).body(image);
    }
}
