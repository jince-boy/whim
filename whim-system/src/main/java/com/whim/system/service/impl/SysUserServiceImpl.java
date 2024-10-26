package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wf.captcha.ArithmeticCaptcha;
import com.whim.common.constant.RedisConstants;
import com.whim.common.utils.IDGeneratorUtil;
import com.whim.common.utils.IPUtil;
import com.whim.common.utils.RedisUtil;
import com.whim.common.utils.StringFormatUtil;
import com.whim.model.dto.LoginDTO;
import com.whim.model.entity.SysUser;
import com.whim.model.vo.CaptchaVO;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.service.ISysUserService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final RedisUtil redisUtil;

    @Override
    public String login(LoginDTO loginDTO) {
        String captchaKey = StringFormatUtil.format(RedisConstants.CAPTCHA_KEY, loginDTO.getUuid(), IPUtil.getClientIpAddress());
        String object = redisUtil.getObject(captchaKey);
        if (StringUtils.isBlank(object)) {
            return "1";
        }
        return "2";
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
        return new CaptchaVO().setUuid(uuid).setBase64(captcha.toBase64());
    }
}

