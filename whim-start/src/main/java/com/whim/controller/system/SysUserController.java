package com.whim.controller.system;

import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.system.model.vo.UserInfoVO;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date: 2025/7/21 12:19
 * @description: 系统用户控制器
 */
@SystemApiPrefix
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {
    private final ISysUserService userService;

    @GetMapping("/getInfo")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success("获取成功", userService.getUserInfo());
    }
}
