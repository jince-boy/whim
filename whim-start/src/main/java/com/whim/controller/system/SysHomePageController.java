package com.whim.controller.system;

import cn.dev33.satoken.annotation.SaMode;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.homepage.HomeTargetQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageAddDTO;
import com.whim.system.model.dto.homepage.SysHomePageQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageUpdateDTO;
import com.whim.system.model.vo.homepage.HomeRouteVO;
import com.whim.system.model.vo.homepage.HomeTargetVO;
import com.whim.system.model.vo.homepage.SysHomePageVO;
import com.whim.system.service.ISysHomePageService;
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

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页管理与当前账号首页接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/homePage")
public class SysHomePageController {
    private final ISysHomePageService homePages;

    /**
     * 分页查询首页配置。
     *
     * @param query 分页和筛选条件
     * @return 首页配置分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:homePage:page")
    public Result<PageDataVO<SysHomePageVO>> getHomePageConfigPage(@Valid SysHomePageQueryDTO query) {
        return Result.success(homePages.getHomePageConfigPage(query));
    }

    /**
     * 搜索可关联的用户、角色或部门。
     *
     * @param query 类型、关键词和分页条件
     * @return 关联对象分页
     */
    @GetMapping("/targets")
    @SystemCheckPermission(value = {"system:homePage:add", "system:homePage:update"}, mode = SaMode.OR)
    public Result<PageDataVO<HomeTargetVO>> getHomeTargetPage(@Valid HomeTargetQueryDTO query) {
        return Result.success(homePages.getHomeTargetPage(query));
    }

    /**
     * 按优先顺序查询当前账号可匹配的首页。
     *
     * @return 首页路由候选
     */
    @GetMapping("/current")
    public Result<List<HomeRouteVO>> getCurrentHomeRoutes() {
        return Result.success(homePages.getCurrentHomeRoutes());
    }

    /**
     * 新增首页配置。
     *
     * @param parameter 首页配置
     * @return 操作结果
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:homePage:add")
    @Log(title = "首页管理", type = LogType.INSERT)
    public Result<Void> addHomePage(@Valid @RequestBody SysHomePageAddDTO parameter) {
        homePages.addHomePage(parameter);
        return Result.success("首页配置已新增");
    }

    /**
     * 更新首页配置。
     *
     * @param parameter 首页配置和主键
     * @return 操作结果
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:homePage:update")
    @Log(title = "首页管理", type = LogType.UPDATE)
    public Result<Void> updateHomePage(@Valid @RequestBody SysHomePageUpdateDTO parameter) {
        homePages.updateHomePage(parameter);
        return Result.success("首页配置已更新");
    }

    /**
     * 删除首页配置。
     *
     * @param id 配置主键
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:homePage:delete")
    @Log(title = "首页管理", type = LogType.DELETE)
    public Result<Void> deleteHomePage(@RequestParam Long id) {
        homePages.deleteHomePage(id);
        return Result.success("首页配置已删除");
    }
}
