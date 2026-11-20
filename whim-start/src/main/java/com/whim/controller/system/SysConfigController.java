package com.whim.controller.system;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * @return 网站标题、介绍和版权信息
     */
    @GetMapping("/site")
    public Result<SiteSettingsVO> getSiteSettings() {
        return Result.success("网站信息查询成功", configService.getSiteSettings());
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
}
