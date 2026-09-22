package com.whim.controller.system;

import com.whim.system.model.dto.user.DefaultTenantDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.service.IAuthService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 当前用户个人设置控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class UserProfileController {

    /**
     * 认证服务对象
     */
    private final IAuthService authService;

    /**
     * 设置当前用户默认进入的租户。
     *
     * @param defaultTenantDTO 默认租户参数
     * @return 更新后的当前用户信息
     */
    @CrossOrigin
    @PutMapping("/defaultTenant")
    public Result<AuthUserVO> defaultTenant(@RequestBody @Valid DefaultTenantDTO defaultTenantDTO) {
        return Result.success("默认租户设置成功", authService.setDefaultTenant(defaultTenantDTO.getTenantId()));
    }
}
