package com.whim.system.service.impl;

import com.wf.captcha.GifCaptcha;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.IdUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.auth.AuthTenantVO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RateType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final Duration CAPTCHA_LIFETIME = Duration.ofMinutes(2);

    private static final String CAPTCHA_KEY_PREFIX = "auth:loginCaptcha:";

    /**
     * 系统用户服务对象
     */
    private final ISysUserService sysUserService;

    /**
     * 系统角色服务对象
     */
    private final ISysRoleService sysRoleService;

    /**
     * 系统租户服务对象
     */
    private final ISysTenantService sysTenantService;

    /**
     * 用户租户关系服务对象
     */
    private final ISysUserTenantService sysUserTenantService;

    /**
     * 当前请求认证上下文
     */
    private final AuthenticationContext authenticationContext;

    /**
     * 认证会话操作对象
     */
    private final AuthenticationSession authenticationSession;

    /**
     * 生成 GIF 验证码，并按 UUID 和客户端地址保存答案。
     *
     * @param clientAddress 客户端地址
     * @return GIF 验证码
     */
    @Override
    public LoginCaptchaVO createCaptcha(String clientAddress) {
        if (RedisUtils.rateLimiter("auth:captchaIssue:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0) {
            throw new TooManyRequestsException("验证码获取过于频繁，请稍后重试");
        }
        GifCaptcha captcha = new GifCaptcha(160, 60, 4);
        String uuid = IdUtils.uuid();
        RedisUtils.setCacheObject(CAPTCHA_KEY_PREFIX + uuid + ":" + clientAddress, captcha.text(), CAPTCHA_LIFETIME);
        LoginCaptchaVO result = new LoginCaptchaVO();
        result.setUuid(uuid);
        result.setImage(captcha.toBase64());
        result.setExpiresIn(CAPTCHA_LIFETIME.toSeconds());
        return result;
    }

    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public AuthenticationToken login(AuthLoginDTO loginDTO) {
        String clientAddress = IPUtils.getClientIpAddress();
        if (RedisUtils.rateLimiter("auth:loginIp:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0
                || RedisUtils.rateLimiter("auth:loginUser:" + loginDTO.getUsername(), RateType.OVERALL, 10, 60, 120) < 0) {
            throw new TooManyRequestsException("登录请求过于频繁，请稍后重试");
        }
        // 验证验证码
        validateCaptcha(loginDTO);
        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
        if (user == null || !BCryptUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new UserPasswordNotMatchException("用户名不存在或密码错误");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }

        return authenticationSession.login(
                sysUserService.buildUserInfo(user),
                Boolean.TRUE.equals(loginDTO.getRememberMe())
        );
    }

    /**
     * 根据 UUID 和客户端地址一次性取出验证码并校验答案。
     *
     * @param loginDTO      登录参数
     */
    private void validateCaptcha(AuthLoginDTO loginDTO) {
        String captchaKey = CAPTCHA_KEY_PREFIX + loginDTO.getUuid() + ":" + IPUtils.getClientIpAddress();
        String expected = RedisUtils.takeCacheObject(captchaKey);
        if (expected == null || !expected.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
            throw new IllegalArgumentException("验证码错误或已过期，请刷新后重试");
        }
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        authenticationSession.logout();
    }

    /**
     * 获取当前用户信息，显式切换租户或保留当前令牌的有效租户。
     *
     * @param tenantId 目标租户ID，不传时保留当前租户；尚未选租户时使用默认租户
     * @return 当前登录用户信息
     */
    @Override
    public AuthUserVO getUserInfo(Long tenantId) {
        SysUser user = sysUserService.getById(authenticationContext.getUserId());
        if (user == null) {
            throw new UserNotFoundException("当前登录用户不存在");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }

        Long selectedTenantId = tenantId != null
                ? tenantId : authenticationContext.getCurrentUserInfo().getCurrentTenantId();
        UserInfo userInfo = selectedTenantId == null
                ? sysUserService.buildUserInfo(user)
                : sysUserService.buildUserInfo(user, selectedTenantId);
        List<SysTenant> tenantList = sysRoleService.isSuperAdministrator(user.getId())
                ? sysTenantService.getAvailableTenantList()
                : sysUserTenantService.getTenantListByUserId(user.getId());
        AuthUserVO userVO = BeanConvertUtils.convert(userInfo, AuthUserVO.class);
        if (userVO != null) {
            userVO.setTenantList(BeanConvertUtils.convertList(tenantList, AuthTenantVO.class));
        }
        authenticationSession.updateUserInfo(userInfo);
        return userVO;
    }

}
