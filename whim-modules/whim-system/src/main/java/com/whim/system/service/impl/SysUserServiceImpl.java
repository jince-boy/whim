package com.whim.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.LoginDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;
import com.whim.system.service.ISysUserService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

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
        SysUser sysUser = this.getSysUserByUsername(loginDTO.getUsername().trim());
        if (Objects.isNull(sysUser)) {
            throw new UserNotFoundException("用户不存在");
        }
        if (!BCryptUtils.matches(loginDTO.getPassword(), sysUser.getPassword())) {
            throw new UserPasswordNotMatchException("用户名或密码错误");
        }
        // 判断是否选中了记住我
        if (loginDTO.getRememberMe()) {
            StpKit.SYSTEM.login(sysUser.getId());
        } else {
            StpKit.SYSTEM.login(sysUser.getId(), false);
        }
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

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return SysUser 系统用户对象
     */
    @Override
    public SysUser getSysUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NullPointerException("username 不能为空");
        }
        return this.lambdaQuery().eq(SysUser::getUsername, username).one();
    }
}

