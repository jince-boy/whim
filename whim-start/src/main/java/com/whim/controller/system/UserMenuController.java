package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.system.model.vo.permission.UserMenuVO;
import com.whim.system.service.impl.UserMenuService;
import com.whim.web.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 登录用户导航入口，页面授权由统一登录上下文决定。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
@SystemCheckLogin
public class UserMenuController {
    private final UserMenuService userMenuService;

    /** 返回当前用户已授权的页面及有效目录树。 */
    @GetMapping("/userMenus")
    public Result<List<UserMenuVO>> getUserMenus() {
        return Result.success("获取用户菜单成功", userMenuService.getUserMenus());
    }
}

