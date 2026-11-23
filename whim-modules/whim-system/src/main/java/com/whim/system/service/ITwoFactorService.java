package com.whim.system.service;

import com.whim.system.model.dto.auth.TwoFactorConfirmDTO;
import com.whim.system.model.dto.auth.TwoFactorDisableDTO;
import com.whim.system.model.dto.auth.TwoFactorSetupDTO;
import com.whim.system.model.dto.user.SysUserTwoFactorResetDTO;
import com.whim.system.model.vo.auth.TwoFactorSetupVO;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 当前用户的两步验证管理。
 */
public interface ITwoFactorService {
    /**
     * 查询当前登录用户是否已完成绑定。
     *
     * @return 是否已启用两步验证
     */
    boolean isTwoFactorEnabled();

    /**
     * 验证当前密码后准备密钥及一次性恢复码，尚未改变登录要求。
     *
     * @param parameter 本人当前密码
     * @return 仅显示一次的绑定资料
     */
    TwoFactorSetupVO startTwoFactorSetup(TwoFactorSetupDTO parameter);

    /**
     * 验证首次动态码，提交绑定后使旧会话失效。
     *
     * @param parameter 本次绑定凭证和首次动态码
     */
    void enableTwoFactor(TwoFactorConfirmDTO parameter);

    /**
     * 核验当前密码和第二因素，关闭本人的两步验证。
     *
     * @param parameter 当前密码及动态码或恢复码
     */
    void disableTwoFactor(TwoFactorDisableDTO parameter);

    /**
     * 核验超级管理员身份及本人密码，解除其他用户的指定验证器绑定。
     *
     * @param parameter 目标用户、预期绑定及操作者当前密码
     */
    void resetUserTwoFactor(SysUserTwoFactorResetDTO parameter);
}
