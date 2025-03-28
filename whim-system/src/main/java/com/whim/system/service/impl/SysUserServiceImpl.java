package com.whim.system.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wf.captcha.ArithmeticCaptcha;
import com.whim.common.constant.RedisConstants;
import com.whim.common.exception.CheckCaptchaException;
import com.whim.common.exception.UserNotFoundException;
import com.whim.common.exception.UserPasswordNotMatchException;
import com.whim.common.utils.BCryptUtil;
import com.whim.common.utils.IDGeneratorUtil;
import com.whim.common.utils.IPUtil;
import com.whim.common.utils.RedisUtil;
import com.whim.common.utils.StringFormatUtil;
import com.whim.model.dto.LoginDTO;
import com.whim.model.entity.SysUser;
import com.whim.model.vo.CaptchaVO;
import com.whim.model.vo.LoginVO;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.service.ISysUserService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final RedisUtil redisUtil;

    /**
     * 用户登录
     *
     * @param loginDTO 用户登录数据传输对象
     * @return 用户登录响应实体类
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 生成验证码的key
        String captchaKey = StringFormatUtil.format(RedisConstants.CAPTCHA_KEY, loginDTO.getUuid(), IPUtil.getClientIpAddress());
        // 获取验证码的值
        String captchaValue = redisUtil.getObject(captchaKey);
        if (StringUtils.isBlank(captchaValue) || !captchaValue.equals(loginDTO.getCaptcha())) {
            throw new CheckCaptchaException("验证码错误");
        }
        // 删除掉通过的验证码
        redisUtil.deleteObject(captchaKey);
        SysUser sysUser = this.getSysUserByUsername(loginDTO.getUsername().trim());
        if (Objects.isNull(sysUser)) {
            throw new UserNotFoundException("用户不存在");
        }
        if (!BCryptUtil.matches(loginDTO.getPassword(), sysUser.getPassword())) {
            throw new UserPasswordNotMatchException("用户名或密码错误");
        }
        // 判断是否选中了记住我
        if (loginDTO.getRememberMe()) {
            StpUtil.login(sysUser.getId());
        } else {
            StpUtil.login(sysUser.getId(), false);
        }
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return new LoginVO("Bearer", tokenInfo.getTokenValue(), tokenInfo.getTokenTimeout());
    }

    /**
     * 获取验证码
     *
     * @return 验证码响应实体
     */
    @Override
    public CaptchaVO getCaptcha() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        String uuid = IDGeneratorUtil.generateUUID();
        //将验证码存入redis中3分钟
        redisUtil.setObject(StringFormatUtil.format(RedisConstants.CAPTCHA_KEY, uuid, IPUtil.getClientIpAddress()), captcha.text(), 3L, TimeUnit.MINUTES);
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

