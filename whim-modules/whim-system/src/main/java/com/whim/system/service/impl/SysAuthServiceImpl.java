package com.whim.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.wf.captcha.ArithmeticCaptcha;
import com.whim.core.constant.RedisConstants;
import com.whim.core.exception.CheckCaptchaException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.IDGeneratorUtils;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.StringFormatUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.satoken.kit.StpKit;
import com.whim.system.model.dto.LoginDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;
import com.whim.system.service.ISysAuthService;
import com.whim.system.service.ISysUserService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * @author jince
 * date: 2025/6/26 15:15
 * description:
 */
@Service
@RequiredArgsConstructor
public class SysAuthServiceImpl implements ISysAuthService {
    private final ISysUserService sysUserService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 生成验证码的key
        String captchaKey = StringFormatUtils.format(RedisConstants.CAPTCHA_KEY, loginDTO.getUuid(), IPUtils.getClientIpAddress());
        // 获取验证码的值
        String captchaValue = RedisUtils.getCacheObject(captchaKey);
        if (StringUtils.isBlank(captchaValue) || !captchaValue.equals(loginDTO.getCaptcha())) {
            throw new CheckCaptchaException("验证码错误");
        }
        // 删除掉通过的验证码
        RedisUtils.deleteObject(captchaKey);
        SysUser sysUser = sysUserService.getSysUserByUsername(loginDTO.getUsername().trim());
        if (Objects.isNull(sysUser)) {
            throw new UserNotFoundException("用户不存在");
        }
        if (!BCryptUtils.matches(loginDTO.getPassword(), sysUser.getPassword())) {
            throw new UserPasswordNotMatchException("用户名或密码错误");
        }
        SaLoginParameter saLoginParameter = new SaLoginParameter();
        if (!loginDTO.getRememberMe()) {
            saLoginParameter.setIsLastingCookie(false);
        }
        saLoginParameter.setExtra("deptId", sysUser.getDeptId());
        StpKit.SYSTEM.login(sysUser.getId(), saLoginParameter);
        StpKit.SYSTEM.getTokenSession().set("loginUserInfo", sysUser);
        SaTokenInfo tokenInfo = StpKit.SYSTEM.getTokenInfo();
        return new LoginVO("Bearer", tokenInfo.getTokenValue(), tokenInfo.getTokenTimeout());
    }

    @Override
    public CaptchaVO getCaptcha() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        String uuid = IDGeneratorUtils.generateUUID();
        RedisUtils.setCacheObject(StringFormatUtils.format(RedisConstants.CAPTCHA_KEY, uuid, IPUtils.getClientIpAddress()), captcha.text(), Duration.ofMinutes(3));
        return new CaptchaVO(uuid, captcha.toBase64());
    }
}
