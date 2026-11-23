package com.whim.controller.monitor;

import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.onlineuser.OnlineUserKickoutDTO;
import com.whim.system.model.dto.onlineuser.OnlineUserQueryDTO;
import com.whim.system.model.vo.onlineuser.OnlineUserPageVO;
import com.whim.system.service.ISysOnlineUserService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 系统监控在线用户接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/monitor/onlineUser")
public class OnlineUserController {
    private final ISysOnlineUserService onlineUserService;

    /**
     * 查询系统账号的在线会话。
     *
     * @param query 筛选和分页条件
     * @return 可见会话分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("monitor:onlineUser:page")
    public Result<OnlineUserPageVO> getOnlineUserPage(@Valid OnlineUserQueryDTO query) {
        return Result.success("在线用户查询成功", onlineUserService.getOnlineUserPage(query));
    }

    /**
     * 强制指定终端重新登录。
     *
     * @param parameter 目标账号和会话标识
     * @return 强退结果
     */
    @PostMapping("/kickout")
    @SystemCheckPermission("monitor:onlineUser:kickout")
    @Log(title = "在线用户强制下线", type = LogType.UPDATE)
    public Result<Void> kickoutOnlineUser(@Valid @RequestBody OnlineUserKickoutDTO parameter) {
        onlineUserService.kickoutOnlineUser(parameter);
        return Result.success("目标会话已下线");
    }
}
