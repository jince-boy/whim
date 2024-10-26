package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.model.dto.LoginDTO;
import com.whim.model.entity.SysUser;
import com.whim.model.vo.CaptchaVO;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务接口
 */
public interface ISysUserService extends IService<SysUser> {

    String login(LoginDTO loginDTO);

    /**
     * 获取验证码
     *
     * @return 验证码响应实体
     */
    CaptchaVO getCaptcha();
}
