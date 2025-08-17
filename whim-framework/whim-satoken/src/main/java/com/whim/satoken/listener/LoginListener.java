package com.whim.satoken.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.whim.core.utils.SpringUtils;
import com.whim.satoken.event.LoginEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jince
 * date: 2025/8/17 13:49
 * description: 登录监听器
 */
@Slf4j
public class LoginListener extends SaTokenListenerForSimple {
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        LoginEvent loginEvent = new LoginEvent(loginParameter.getExtra("username").toString(),0,"登录成功");
        // 发送登录事件
        SpringUtils.getApplicationContext().publishEvent(loginEvent);
    }
}
