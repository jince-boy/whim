package com.whim.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.wf.captcha.ArithmeticCaptcha;
import com.whim.core.constant.RedisConstants;
import com.whim.core.constant.SystemConstants;
import com.whim.core.exception.CheckCaptchaException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.ConvertUtils;
import com.whim.core.utils.IDGeneratorUtils;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.SpringUtils;
import com.whim.core.utils.StringFormatUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.satoken.core.context.AuthContext;
import com.whim.satoken.core.logic.StpAuthManager;
import com.whim.satoken.core.model.RoleInfo;
import com.whim.satoken.core.model.UserInfo;
import com.whim.satoken.event.LoginEvent;
import com.whim.satoken.service.PermissionProvider;
import com.whim.system.model.dto.auth.LoginDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;
import com.whim.system.service.ISysAuthService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * @author jince
 * @date 2025/6/26 15:15
 * @description 系统用户认证
 */
@Service
@RequiredArgsConstructor
public class SysAuthServiceImpl implements ISysAuthService {
    private final ISysUserService sysUserService;
    private final PermissionProvider permissionProvider;
    private final ISysRoleService sysRoleService;

    /**
     * 用户登录
     *
     * @param loginDTO 用户登录数据传输对象
     * @return 用户登录响应实体类
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 验证码校验
        this.validateCaptcha(loginDTO);
        // 用户校验
        SysUser sysUser = this.validateUser(loginDTO);
        // 状态校验
        this.validateUserStatus(sysUser);
        // 构建用户信息
        UserInfo userInfo = this.buildUserInfo(sysUser);
        // 执行登录并返回结果
        return this.doLogin(userInfo);
    }

    /**
     * 校验验证码
     */
    private void validateCaptcha(LoginDTO loginDTO) {
        // 生成验证码的key
        String captchaKey = StringFormatUtils.format(RedisConstants.CAPTCHA_KEY, loginDTO.getUuid(), IPUtils.getClientIpAddress());
        // 获取验证码的值
        String captchaValue = RedisUtils.getCacheObject(captchaKey);
        if (Objects.isNull(captchaValue)) {
            throw new CheckCaptchaException("验证码已过期");
        }
        if (!captchaValue.equals(loginDTO.getCaptcha())) {
            this.handleLoginFailure(loginDTO.getUsername(), "验证码错误", new CheckCaptchaException("验证码错误"));
        }
        // 删除掉通过的验证码
        RedisUtils.deleteObject(captchaKey);
    }

    /**
     * 校验用户
     */
    private SysUser validateUser(LoginDTO loginDTO) {
        SysUser sysUser = sysUserService.getSysUserByUsername(loginDTO.getUsername().trim());
        if (Objects.isNull(sysUser)) {
            this.handleLoginFailure(loginDTO.getUsername(), "用户不存在", new UserNotFoundException("用户不存在"));
        }
        if (!BCryptUtils.matches(loginDTO.getPassword(), sysUser.getPassword())) {
            handleLoginFailure(loginDTO.getUsername(), "用户名或密码错误", new UserPasswordNotMatchException("用户名或密码错误"));
        }
        return sysUser;
    }

    /**
     * 校验用户状态
     */
    private void validateUserStatus(SysUser sysUser) {
        if (sysUser.getStatus() == 1) {
            this.handleLoginFailure(sysUser.getUsername(), "用户被禁用", new UserDisableException("当前用户被禁用"));
        }
        if (StpAuthManager.SYSTEM.isDisable(sysUser.getId())) {
            long disableTime = StpAuthManager.SYSTEM.getDisableTime(sysUser.getId());
            this.handleLoginFailure(sysUser.getUsername(), "用户被禁用,剩余封禁时间:" + disableTime + "秒",
                    new UserDisableException("用户被禁用,剩余封禁时间:" + disableTime + "秒"));
        }
    }

    /**
     * 构建UserInfo对象
     */
    private UserInfo buildUserInfo(SysUser sysUser) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(sysUser.getId());
        userInfo.setUsername(sysUser.getUsername());
        userInfo.setDeptId(sysUser.getDeptId());
        userInfo.setPermissionCodeSet(permissionProvider.getPermissionList(sysUser.getId()));
        userInfo.setRoleCodeSet(permissionProvider.getRoleList(sysUser.getId()));
        userInfo.setRoleInfoList(ConvertUtils.convert(sysRoleService.getRoleInfoListByUserId(sysUser.getId()), RoleInfo.class));
        return userInfo;
    }

    private LoginVO doLogin(UserInfo userInfo) {
        SaLoginParameter saLoginParameter = new SaLoginParameter();
        saLoginParameter.setExtra("username", userInfo.getUsername());
        saLoginParameter.setIsLastingCookie(false);
        AuthContext.login(userInfo, saLoginParameter);
        SaTokenInfo tokenInfo = StpAuthManager.SYSTEM.getTokenInfo();
        return new LoginVO("Bearer", tokenInfo.getTokenValue(), tokenInfo.getTokenTimeout());
    }

    /**
     * 发布登录事件
     */
    private void handleLoginFailure(String username, String message, RuntimeException ex) {
        SpringUtils.getApplicationContext().publishEvent(new LoginEvent(username, SystemConstants.STATUS_DISABLE, message));
        throw ex;
    }

    /**
     * 退出登录
     */
    @Override
    public void logout() {
        StpAuthManager.SYSTEM.logout();
    }

    /**
     * 获取验证码
     *
     * @return 验证码响应实体
     */
    @Override
    public CaptchaVO getCaptcha() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        String uuid = IDGeneratorUtils.generateUUID();
        RedisUtils.setCacheObject(StringFormatUtils.format(RedisConstants.CAPTCHA_KEY, uuid, IPUtils.getClientIpAddress()), captcha.text(), Duration.ofMinutes(3));
        return new CaptchaVO(uuid, captcha.toBase64());
    }
}
