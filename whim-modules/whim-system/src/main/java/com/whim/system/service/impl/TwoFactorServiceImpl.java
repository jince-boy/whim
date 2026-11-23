package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.authenticator.recovery.RecoveryCodes;
import com.whim.authenticator.totp.TotpCredential;
import com.whim.authenticator.totp.TotpEnrollment;
import com.whim.authenticator.totp.TotpVerificationService;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.utils.BCryptUtils;
import com.whim.system.mapper.SysUserTwoFactorMapper;
import com.whim.system.model.dto.auth.TwoFactorConfirmDTO;
import com.whim.system.model.dto.auth.TwoFactorDisableDTO;
import com.whim.system.model.dto.auth.TwoFactorSetupDTO;
import com.whim.system.model.dto.user.SysUserTwoFactorResetDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserTwoFactor;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.auth.TwoFactorSetupVO;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ITwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalLong;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 管理本人验证器的绑定、动态码消费和恢复。
 */
@Service
@RequiredArgsConstructor
public class TwoFactorServiceImpl extends ServiceImpl<SysUserTwoFactorMapper, SysUserTwoFactor> implements ITwoFactorService {
    private final ISysUserService userService;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final TwoFactorChallengeService twoFactorChallenges;
    private final TotpVerificationService totpVerification;

    /**
     * 查询指定用户的有效绑定，未开启时返回空。
     *
     * @param userId 用户主键
     * @return 当前绑定
     */
    private SysUserTwoFactor getBinding(Long userId) {
        return lambdaQuery().select(SysUserTwoFactor::getId, SysUserTwoFactor::getUserId, SysUserTwoFactor::getLastStep,
                        SysUserTwoFactor::getEncryptedSecret, SysUserTwoFactor::getRecoveryHash)
                .eq(SysUserTwoFactor::getUserId, userId).one();
    }

    /**
     * 查询有效绑定编号，密码登录和绑定状态检查只读取标识。
     *
     * @param userId 用户主键
     * @return 当前绑定编号，未绑定时为空
     */
    Long getBindingId(Long userId) {
        SysUserTwoFactor binding = lambdaQuery().select(SysUserTwoFactor::getId)
                .eq(SysUserTwoFactor::getUserId, userId).one();
        return binding == null ? null : binding.getId();
    }

    /**
     * 查询当前登录用户是否已完成绑定。
     *
     * @return 是否已启用两步验证
     */
    @Override
    public boolean isTwoFactorEnabled() {
        return getBindingId(authenticationContext.getUserId()) != null;
    }

    /**
     * 串行处理同一账号的登录、绑定和找回，重新确认账号有效。
     *
     * @param userId 已通过第一步认证的账号主键
     * @return 加锁后的最新账号
     */
    SysUser lockAccount(Long userId) {
        // 绑定前没有凭据行可锁；锁定一直存在的用户行，使首次启用也和密码登录互斥。
        SysUser user = userService.lambdaQuery().eq(SysUser::getId, userId).last("FOR UPDATE").one();
        if (user == null || SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new IllegalArgumentException("账号已不可用，请重新登录");
        }
        return user;
    }

    /**
     * 验证当前密码后准备密钥及一次性恢复码，尚未改变登录要求。
     *
     * @param parameter 本人当前密码
     * @return 仅显示一次的绑定资料
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TwoFactorSetupVO startTwoFactorSetup(TwoFactorSetupDTO parameter) {
        Long userId = authenticationContext.getUserId();
        twoFactorChallenges.limitAttempts(userId);
        SysUser user = lockAccountWithPassword(userId, parameter.getPassword());
        if (getBindingId(userId) != null) {
            throw new IllegalArgumentException("两步验证已开启");
        }
        TotpEnrollment enrollment = totpVerification.startEnrollment(userId, user.getPassword());
        TwoFactorSetupVO setup = new TwoFactorSetupVO();
        setup.setSetupId(enrollment.getSetupId());
        setup.setSecret(enrollment.getSecret());
        setup.setRecoveryCode(enrollment.getRecoveryCode());
        return setup;
    }

    /**
     * 验证首次动态码，提交绑定后使旧会话失效。
     *
     * @param parameter 本次绑定凭证和首次动态码
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void enableTwoFactor(TwoFactorConfirmDTO parameter) {
        Long userId = authenticationContext.getUserId();
        twoFactorChallenges.limitAttempts(userId);
        SysUser user = lockAccount(userId);
        if (getBindingId(userId) != null) {
            throw new IllegalArgumentException("两步验证已开启");
        }
        TotpCredential credential = totpVerification.verifyEnrollment(
                userId, user.getPassword(), parameter.getSetupId(), parameter.getCode());
        SysUserTwoFactor binding = new SysUserTwoFactor();
        binding.setUserId(userId);
        binding.setEncryptedSecret(credential.getEncryptedSecret());
        binding.setRecoveryHash(credential.getRecoveryHash());
        binding.setLastStep(credential.getLastStep());
        if (!save(binding)) {
            throw new ServiceException("保存两步验证绑定失败");
        }
        totpVerification.clearEnrollment(userId);
        // 此前仅凭密码建立的全部会话在提交成功后重新认证。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /**
     * 核验当前密码和第二因素，关闭本人的两步验证。
     *
     * @param parameter 当前密码及动态码或恢复码
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void disableTwoFactor(TwoFactorDisableDTO parameter) {
        Long userId = authenticationContext.getUserId();
        twoFactorChallenges.limitAttempts(userId);
        lockAccountWithPassword(userId, parameter.getPassword());
        SysUserTwoFactor binding = getBinding(userId);
        if (binding == null) {
            throw new IllegalArgumentException("两步验证尚未开启");
        }
        verifySecondFactor(binding, parameter.getCode());
        removeBinding(binding);
    }

    /**
     * 核验超级管理员身份及本人密码，解除其他用户的指定验证器绑定。
     *
     * @param parameter 目标用户、预期绑定及操作者当前密码
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void resetUserTwoFactor(SysUserTwoFactorResetDTO parameter) {
        // 身份来自服务端登录快照，角色权限不能授予此恢复能力。
        if (!authenticationContext.isSuperAdministrator()) {
            throw new AccessDeniedException("仅超级管理员可以重置用户的两步验证");
        }
        Long operatorId = authenticationContext.getUserId();
        if (operatorId.equals(parameter.getId())) {
            throw new IllegalArgumentException("请在个人信息页管理本人的两步验证");
        }
        twoFactorChallenges.limitAttempts(operatorId);
        SysUser operator = userService.lambdaQuery().select(SysUser::getStatus, SysUser::getPassword)
                .eq(SysUser::getId, operatorId).one();
        if (operator == null || SysUserStatus.DISABLED.matches(operator.getStatus())) {
            throw new AccessDeniedException("当前账号不能执行两步验证重置");
        }
        if (!BCryptUtils.matches(parameter.getCurrentPassword(), operator.getPassword())) {
            throw new IllegalArgumentException("超级管理员的当前密码不正确");
        }
        // 与本人绑定、登录共用用户行锁；停用的目标也可解除绑定，找回操作不会启用账号。
        SysUser target = userService.lambdaQuery().select(SysUser::getId)
                .eq(SysUser::getId, parameter.getId()).last("FOR UPDATE").one();
        if (target == null) {
            throw new UserNotFoundException("目标用户不存在");
        }
        SysUserTwoFactor binding = lambdaQuery().select(SysUserTwoFactor::getId, SysUserTwoFactor::getUserId)
                .eq(SysUserTwoFactor::getUserId, target.getId())
                .eq(SysUserTwoFactor::getId, parameter.getBindingId()).one();
        // 绑定在弹窗打开后可能被替换，旧请求及重试均不能移除用户新绑定的验证器。
        if (binding == null) {
            throw new IllegalArgumentException("两步验证绑定已变更，请刷新用户列表");
        }
        removeBinding(binding);
    }

    /**
     * 在用户行锁下完成第二步登录，消费短时凭证并记录动态码或撤销已找回的绑定。
     *
     * @param user 当前加锁的系统账号
     * @param challenge 密码验证后签发的临时登录凭证
     * @param code 六位动态码或一次性恢复码
     * @return 是否已通过恢复码解除旧绑定
     */
    boolean completeLoginChallenge(SysUser user, TwoFactorChallengeService.LoginChallenge challenge, String code) {
        SysUserTwoFactor binding = getBinding(user.getId());
        if (binding == null || !challenge.matchesSecurityState(user.getPassword(), binding.getId())) {
            throw new IllegalArgumentException("账号安全设置已变更，请返回密码登录");
        }
        OptionalLong step = verifySecondFactor(binding, code);
        // 先确认短时凭证仍有效且只消费一次，再在当前事务内更新或撤销绑定。
        challenge.consume();
        if (step.isEmpty()) {
            removeBinding(binding);
            return true;
        }
        SysUserTwoFactor usage = new SysUserTwoFactor();
        usage.setId(binding.getId());
        usage.setLastStep(step.getAsLong());
        if (!updateById(usage)) {
            throw new ServiceException("保存动态码使用状态失败");
        }
        return false;
    }

    /**
     * 校验当前绑定的第二因素，解绑时无需再写入即将删除的动态码使用记录。
     *
     * @param binding 当前有效绑定
     * @param code 六位动态码或一次性恢复码
     * @return 动态码通过时返回使用的时间步，恢复码通过时为空
     */
    private OptionalLong verifySecondFactor(SysUserTwoFactor binding, String code) {
        // 恢复码只依赖绑定摘要，验证器主密钥不可用时仍可找回。
        if (RecoveryCodes.verifyIfRecoveryCode(code, binding.getRecoveryHash())) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(totpVerification.verifyCode(
                binding.getUserId(), binding.getEncryptedSecret(), binding.getLastStep(), code));
    }

    /**
     * 锁定本人账号并核验当前密码，确保绑定变更与改密按顺序生效。
     *
     * @param userId 当前用户主键
     * @param password 本人提交的当前密码
     * @return 已验证并加锁的账号
     */
    private SysUser lockAccountWithPassword(Long userId, String password) {
        SysUser user = lockAccount(userId);
        if (!BCryptUtils.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        return user;
    }

    /**
     * 移除账号旧绑定，提交后使全部已有会话失效。
     *
     * @param binding 已经校验第二因素的绑定
     */
    private void removeBinding(SysUserTwoFactor binding) {
        if (!removeById(binding.getId())) {
            throw new ServiceException("解除两步验证绑定失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(binding.getUserId()));
    }
}
