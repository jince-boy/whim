package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.wf.captcha.GifCaptcha;
import com.whim.authenticator.recovery.RecoveryCodes;
import com.whim.authenticator.totp.TotpCredential;
import com.whim.authenticator.totp.TotpEnrollment;
import com.whim.authenticator.totp.TotpVerificationService;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.core.auth.model.PostInfo;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.IdUtils;
import com.whim.log.enums.LogStatus;
import com.whim.log.publisher.LoginLogPublisher;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.redis.utils.RedisUtils;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.dto.auth.TwoFactorConfirmDTO;
import com.whim.system.model.dto.auth.TwoFactorDisableDTO;
import com.whim.system.model.dto.auth.TwoFactorLoginDTO;
import com.whim.system.model.dto.auth.TwoFactorSetupDTO;
import com.whim.system.model.dto.user.SysUserTwoFactorResetDTO;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserTwoFactor;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.model.vo.auth.LoginResultVO;
import com.whim.system.model.vo.auth.TwoFactorSetupVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysConfigService;
import com.whim.system.service.ISysNoticeService;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RateType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 系统账号登录、验证器绑定与恢复及用户授权快照构建。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final Duration CAPTCHA_LIFETIME = Duration.ofMinutes(2);

    private static final String CAPTCHA_KEY_PREFIX = "auth:loginCaptcha:";
    private static final String TWO_FACTOR_LOGIN_PREFIX = "auth:twoFactorLogin:";
    private static final String TWO_FACTOR_ATTEMPTS_PREFIX = "auth:twoFactorAttempts:";

    /**
     * 系统用户服务对象
     */
    private final ISysUserService sysUserService;
    private final ISysConfigService configService;
    private final ISysNoticeService noticeService;

    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final SysUserMapper userMapper;

    /**
     * 当前请求认证上下文
     */
    private final AuthenticationContext authenticationContext;

    /**
     * 认证会话操作对象
     */
    private final AuthenticationSession authenticationSession;
    private final LoginLogPublisher loginLogPublisher;
    private final TotpVerificationService totpVerification;

    /**
     * 查询当前账号资料，并刷新部门、岗位、角色及完整授权快照。
     *
     * @return 当前登录用户信息
     */
    @Override
    public AuthUserVO getUserInfo() {
        SysUser user = sysUserService.getById(authenticationContext.getUserId());
        if (user == null) {
            throw new UserNotFoundException("当前登录用户不存在");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }
        UserInfo userInfo = buildUserInfo(user);
        authenticationSession.updateUserInfo(userInfo);
        AuthUserVO result = BeanConvertUtils.convert(userInfo, AuthUserVO.class);
        // 个人资料只返回给当前账号，不向公共认证快照加入联系方式或重复查询组织信息。
        result.setPhone(user.getPhone());
        result.setEmail(user.getEmail());
        result.setGender(user.getGender());
        result.setDeptName(userInfo.getDepartmentInfo() == null ? null : userInfo.getDepartmentInfo().getDeptName());
        result.setPostNames(userInfo.getPostInfoList().stream().map(PostInfo::getPostName).toList());
        return result;
    }

    /**
     * 修改当前登录用户的个人资料，并返回更新后的资料与当前会话信息。
     *
     * @param parameter 允许本人维护的个人资料
     * @return 更新后的当前用户信息
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuthUserVO updateUserProfile(AuthProfileUpdateDTO parameter) {
        Long userId = authenticationContext.getUserId();
        SysUser originalUser = sysUserService.getById(userId);
        if (originalUser == null) {
            throw new UserNotFoundException("当前登录用户不存在");
        }
        if (SysUserStatus.DISABLED.matches(originalUser.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }
        SysUser user = BeanConvertUtils.convert(parameter, SysUser.class);
        user.setId(userId);
        user.setAvatar(parameter.getAvatar() == null ? null : parameter.getAvatar().replace("&#61;", "="));
        // 部门和备注配置了ALWAYS更新策略，必须保留原值；共用组织锁避免覆盖管理员并发修改。
        user.setDeptId(originalUser.getDeptId());
        user.setRemark(originalUser.getRemark());
        // 传入实体让元对象处理器自动填写修改人和修改时间，不接收或手动填写审计字段。
        if (!sysUserService.updateById(user)) {
            throw new ServiceException("保存个人资料失败");
        }
        // 单表更新已提交后才刷新当前令牌快照，避免事务回滚时会话保留未提交的姓名或头像。
        return getUserInfo();
    }

    /**
     * 校验原密码后修改本人密码，提交成功后使本人全部登录会话失效。
     *
     * @param parameter 原密码和新密码
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(AuthPasswordUpdateDTO parameter) {
        Long userId = authenticationContext.getUserId();
        // 按账号限制原密码验证频率，避免切换客户端地址后反复猜测密码。
        if (RedisUtils.rateLimiter("auth:passwordUpdate:" + userId, RateType.OVERALL, 5, 60, 120) < 0) {
            throw new TooManyRequestsException("密码修改操作过于频繁，请稍后重试");
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new UserNotFoundException("当前登录用户不存在");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }
        // 原密码输错属于本次提交错误，不使用会触发前端退出登录的认证异常。
        if (!BCryptUtils.matches(parameter.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (parameter.getOldPassword().equals(parameter.getNewPassword())) {
            throw new IllegalArgumentException("新密码不能与原密码相同");
        }
        // 身份校验已读取完整记录，只修改密码，保留所有允许清空的资料和组织字段。
        user.setPassword(BCryptUtils.encode(parameter.getNewPassword()));
        if (!sysUserService.updateById(user)) {
            throw new ServiceException("修改密码失败");
        }
        noticeService.addSystemMessages(Set.of(userId), "登录密码已修改", "你的登录密码已修改，旧密码已失效。");
        // 密码修改与管理员重置保持一致：事务成功后失效全部旧会话，回滚不影响登录。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /**
     * 查询验证码开关，启用时签发与客户端地址绑定的 GIF 验证码。
     *
     * @param clientAddress 客户端地址
     * @return 验证码开关状态，启用时包含 GIF 图片及其有效期
     */
    @Override
    public LoginCaptchaVO createCaptcha(String clientAddress) {
        if (RedisUtils.rateLimiter("auth:captchaIssue:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0) {
            throw new TooManyRequestsException("验证码获取过于频繁，请稍后重试");
        }
        LoginCaptchaVO result = new LoginCaptchaVO();
        result.setEnabled(configService.isLoginCaptchaEnabled());
        // 关闭时只告知页面当前状态，避免签发无用的挑战和 Redis 记录。
        if (!result.isEnabled()) {
            return result;
        }
        GifCaptcha captcha = new GifCaptcha(160, 60, 4);
        String uuid = IdUtils.uuid();
        RedisUtils.setCacheObject(CAPTCHA_KEY_PREFIX + uuid + ":" + clientAddress, captcha.text(), CAPTCHA_LIFETIME);
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
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public LoginResultVO login(AuthLoginDTO loginDTO) {
        String clientAddress = IPUtils.getClientIpAddress();
        if (RedisUtils.rateLimiter("auth:loginIp:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0
                || RedisUtils.rateLimiter("auth:loginUser:" + loginDTO.getUsername(), RateType.OVERALL, 10, 60, 120) < 0) {
            throw new TooManyRequestsException("登录请求过于频繁，请稍后重试");
        }
        // 通过限流的认证尝试只发布一次结果，异常详情及登录凭据不进入事件。
        LogStatus status = LogStatus.FAILURE;
        String remark = "认证服务异常";
        Long credentialFailureUserId = null;
        try {
            String captchaError = getCaptchaError(loginDTO, clientAddress);
            if (captchaError != null) {
                remark = captchaError;
                throw new IllegalArgumentException(remark);
            }
            SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
            if (user == null || !BCryptUtils.matches(loginDTO.getPassword(), user.getPassword())) {
                // 仅已存在账号的凭据错误参与安全提醒，客户端仍收到相同的认证结果。
                credentialFailureUserId = user == null ? null : user.getId();
                remark = "用户名不存在或密码错误";
                throw new UserPasswordNotMatchException(remark);
            }
            if (SysUserStatus.DISABLED.matches(user.getStatus())) {
                remark = "用户已被禁用";
                throw new UserDisableException(remark);
            }
            // 密码验证期间可能发生改密、启停或首次绑定，签发前在同一账号行锁下读取最新状态。
            SysUser current = lockActiveUser(user.getId());
            if (!current.getPassword().equals(user.getPassword())) {
                throw new IllegalArgumentException("密码已变更，请重新登录");
            }
            Long bindingId = getTwoFactorBindingId(current.getId());
            if (bindingId != null) {
                LoginResultVO pending = new LoginResultVO();
                pending.setChallengeId(issueLoginChallenge(current, bindingId, clientAddress));
                // 密码通过只进入待验证状态，成功日志与正式令牌一起在第二步产生。
                remark = null;
                return pending;
            }
            LoginResultVO token = completeLogin(current, clientAddress);
            status = LogStatus.SUCCESS;
            remark = "登录成功";
            return token;
        } finally {
            if (remark != null) {
                publishLoginResult(loginDTO.getUsername(), clientAddress, status, remark, credentialFailureUserId);
            }
        }
    }

    /**
     * 核验短时登录凭证及第二因素，恢复码仅用于解除丢失的验证器。
     *
     * @param parameter 密码挑战凭证和动态码或恢复码
     * @return 正式登录结果或恢复完成状态
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public LoginResultVO verifyLogin(TwoFactorLoginDTO parameter) {
        String address = IPUtils.getClientIpAddress();
        String challengeKey = TWO_FACTOR_LOGIN_PREFIX + parameter.getChallengeId() + ":" + address;
        Map<String, String> challenge = RedisUtils.getCacheObject(challengeKey);
        if (challenge == null) {
            throw new IllegalArgumentException("登录验证已过期，请返回密码登录");
        }
        Long userId = Long.valueOf(challenge.get("userId"));
        limitTwoFactorAttempts(userId);
        SysUser user = lockActiveUser(userId);
        LogStatus status = LogStatus.FAILURE;
        String remark = "两步验证失败";
        Long credentialFailureUserId = null;
        try {
            SysUserTwoFactor binding = getTwoFactorBinding(userId);
            // 改密或重新绑定后，第一步签发的凭证不能继续用于登录。
            if (binding == null || !getLoginChallengeProof(user.getPassword(), binding.getId()).equals(challenge.get("proof"))) {
                throw new IllegalArgumentException("账号安全设置已变更，请返回密码登录");
            }
            OptionalLong step;
            try {
                step = verifySecondFactor(binding, parameter.getCode());
            } catch (IllegalArgumentException exception) {
                credentialFailureUserId = userId;
                throw exception;
            }
            // 在账号行锁下原子消费凭证，失败的动态码可以重试，过期或已消费的凭证不能换取会话。
            if (RedisUtils.takeCacheObject(challengeKey) == null) {
                throw new IllegalArgumentException("登录验证已过期，请返回密码登录");
            }
            if (step.isEmpty()) {
                removeTwoFactorBinding(binding);
                noticeService.addSystemMessages(Set.of(userId), "已使用恢复码解除两步验证", "你的账号已使用恢复码解除原验证器绑定。请在个人信息页重新绑定验证器。");
                LoginResultVO result = new LoginResultVO();
                result.setRecovered(true);
                // 成功找回不会签发令牌，不计入登录成功或失败；审计只记录账号标识。
                log.info("已通过恢复码验证本人身份，用户ID={}", userId);
                remark = null;
                return result;
            }
            SysUserTwoFactor usage = new SysUserTwoFactor();
            usage.setId(binding.getId());
            usage.setLastStep(step.getAsLong());
            if (!Db.updateById(usage)) {
                throw new ServiceException("保存动态码使用状态失败");
            }
            LoginResultVO result = completeLogin(user, address);
            status = LogStatus.SUCCESS;
            remark = "两步验证登录成功";
            return result;
        } finally {
            if (remark != null) {
                publishLoginResult(user.getUsername(), address, status, remark, credentialFailureUserId);
            }
        }
    }

    /**
     * 查询当前登录用户是否已完成绑定。
     *
     * @return 是否已启用两步验证
     */
    @Override
    public boolean isTwoFactorEnabled() {
        return getTwoFactorBindingId(authenticationContext.getUserId()) != null;
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
        limitTwoFactorAttempts(userId);
        SysUser user = lockActiveUserWithPassword(userId, parameter.getPassword());
        if (getTwoFactorBindingId(userId) != null) {
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
        limitTwoFactorAttempts(userId);
        SysUser user = lockActiveUser(userId);
        if (getTwoFactorBindingId(userId) != null) {
            throw new IllegalArgumentException("两步验证已开启");
        }
        TotpCredential credential = totpVerification.verifyEnrollment(
                userId, user.getPassword(), parameter.getSetupId(), parameter.getCode());
        SysUserTwoFactor binding = new SysUserTwoFactor();
        binding.setUserId(userId);
        binding.setEncryptedSecret(credential.getEncryptedSecret());
        binding.setRecoveryHash(credential.getRecoveryHash());
        binding.setLastStep(credential.getLastStep());
        if (!Db.save(binding)) {
            throw new ServiceException("保存两步验证绑定失败");
        }
        noticeService.addSystemMessages(Set.of(userId), "已开启两步验证", "你的账号已绑定验证器，今后登录时需要输入动态码。");
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
        limitTwoFactorAttempts(userId);
        lockActiveUserWithPassword(userId, parameter.getPassword());
        SysUserTwoFactor binding = getTwoFactorBinding(userId);
        if (binding == null) {
            throw new IllegalArgumentException("两步验证尚未开启");
        }
        boolean usedRecoveryCode = verifySecondFactor(binding, parameter.getCode()).isEmpty();
        removeTwoFactorBinding(binding);
        String content = usedRecoveryCode ? "你已使用恢复码关闭两步验证，原验证器绑定已解除。" : "你已关闭两步验证，原验证器绑定已解除。";
        noticeService.addSystemMessages(Set.of(userId), "已关闭两步验证", content);
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
        limitTwoFactorAttempts(operatorId);
        SysUser operator = sysUserService.lambdaQuery().select(SysUser::getStatus, SysUser::getPassword)
                .eq(SysUser::getId, operatorId).one();
        if (operator == null || SysUserStatus.DISABLED.matches(operator.getStatus())) {
            throw new AccessDeniedException("当前账号不能执行两步验证重置");
        }
        if (!BCryptUtils.matches(parameter.getCurrentPassword(), operator.getPassword())) {
            throw new IllegalArgumentException("超级管理员的当前密码不正确");
        }
        // 与本人绑定、登录共用用户行锁；停用的目标也可解除绑定，找回操作不会启用账号。
        SysUser target = sysUserService.lambdaQuery().select(SysUser::getId)
                .eq(SysUser::getId, parameter.getId()).last("FOR UPDATE").one();
        if (target == null) {
            throw new UserNotFoundException("目标用户不存在");
        }
        SysUserTwoFactor binding = Db.lambdaQuery(SysUserTwoFactor.class).select(SysUserTwoFactor::getId, SysUserTwoFactor::getUserId)
                .eq(SysUserTwoFactor::getUserId, target.getId())
                .eq(SysUserTwoFactor::getId, parameter.getBindingId()).one();
        // 绑定在弹窗打开后可能被替换，旧请求及重试均不能移除用户新绑定的验证器。
        if (binding == null) {
            throw new IllegalArgumentException("两步验证绑定已变更，请刷新用户列表");
        }
        removeTwoFactorBinding(binding);
        noticeService.addSystemMessages(Set.of(target.getId()), "两步验证已被管理员重置", "管理员已解除你的验证器绑定。请在个人信息页重新绑定验证器。");
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        authenticationSession.logout();
    }

    /**
     * 查询指定用户的有效绑定，未开启时返回空。
     *
     * @param userId 用户主键
     * @return 当前绑定
     */
    private SysUserTwoFactor getTwoFactorBinding(Long userId) {
        return Db.lambdaQuery(SysUserTwoFactor.class).select(SysUserTwoFactor::getId, SysUserTwoFactor::getUserId, SysUserTwoFactor::getLastStep,
                        SysUserTwoFactor::getEncryptedSecret, SysUserTwoFactor::getRecoveryHash)
                .eq(SysUserTwoFactor::getUserId, userId).one();
    }

    /**
     * 查询有效绑定编号，密码登录和绑定状态检查只读取标识。
     *
     * @param userId 用户主键
     * @return 当前绑定编号，未绑定时为空
     */
    private Long getTwoFactorBindingId(Long userId) {
        SysUserTwoFactor binding = Db.lambdaQuery(SysUserTwoFactor.class).select(SysUserTwoFactor::getId)
                .eq(SysUserTwoFactor::getUserId, userId).one();
        return binding == null ? null : binding.getId();
    }

    /**
     * 串行处理同一账号的登录、绑定和找回，重新确认账号有效。
     *
     * @param userId 已通过第一步认证的账号主键
     * @return 加锁后的最新账号
     */
    private SysUser lockActiveUser(Long userId) {
        // 绑定前没有凭据行可锁；锁定一直存在的用户行，使首次启用也和密码登录互斥。
        SysUser user = sysUserService.lambdaQuery().eq(SysUser::getId, userId).last("FOR UPDATE").one();
        if (user == null || SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new IllegalArgumentException("账号已不可用，请重新登录");
        }
        return user;
    }

    /**
     * 锁定本人账号并核验当前密码，确保绑定变更与改密按顺序生效。
     *
     * @param userId 当前用户主键
     * @param password 本人提交的当前密码
     * @return 已验证并加锁的账号
     */
    private SysUser lockActiveUserWithPassword(Long userId, String password) {
        SysUser user = lockActiveUser(userId);
        if (!BCryptUtils.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        return user;
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
     * 移除账号旧绑定，提交后使全部已有会话失效。
     *
     * @param binding 已经校验第二因素的绑定
     */
    private void removeTwoFactorBinding(SysUserTwoFactor binding) {
        if (!Db.removeById(binding.getId(), SysUserTwoFactor.class)) {
            throw new ServiceException("解除两步验证绑定失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(binding.getUserId()));
    }

    /**
     * 签发与客户端地址及当前账号安全状态绑定的短时登录凭证。
     *
     * @param user 已通过密码验证的系统账号
     * @param bindingId 密码验证时的验证器绑定编号
     * @param clientAddress 发起请求的客户端地址
     * @return 客户端继续验证时使用的凭证编号
     */
    private String issueLoginChallenge(SysUser user, Long bindingId, String clientAddress) {
        String challengeId = IdUtils.uuid32();
        Map<String, String> challenge = new HashMap<>();
        challenge.put("userId", user.getId().toString());
        challenge.put("proof", getLoginChallengeProof(user.getPassword(), bindingId));
        RedisUtils.setCacheObject(TWO_FACTOR_LOGIN_PREFIX + challengeId + ":" + clientAddress, challenge, Duration.ofMinutes(3));
        return challengeId;
    }

    /**
     * 限制同一账号在登录验证、绑定和找回过程中的尝试总数。
     *
     * @param userId 尝试验证的账号
     */
    private void limitTwoFactorAttempts(Long userId) {
        if (RedisUtils.rateLimiter(TWO_FACTOR_ATTEMPTS_PREFIX + userId, RateType.OVERALL, 8, 60, 120) < 0) {
            throw new TooManyRequestsException("验证过于频繁，请稍后再试");
        }
    }

    /**
     * 将临时凭证绑定到密码和当前验证器，改密或重新绑定会使原凭证失效。
     *
     * @param passwordHash 账号当前的密码摘要
     * @param bindingId 验证器绑定编号
     * @return 登录安全状态摘要
     */
    private static String getLoginChallengeProof(String passwordHash, Long bindingId) {
        return DigestUtils.sha256Hex(passwordHash + ":" + bindingId);
    }

    /**
     * 全部认证步骤通过后签发正式会话，并在登录事务中保存本人提醒。
     *
     * @param user 已通过全部认证的账号
     * @param address 此次登录的客户端地址
     * @return 正式登录结果
     */
    private LoginResultVO completeLogin(SysUser user, String address) {
        UserInfo userInfo = buildUserInfo(user);
        // 先写消息再签发令牌，签发失败时消息随登录事务回滚。
        String content = address == null ? "你的账号已成功登录。" : "你的账号已成功登录，登录 IP 为 " + address + "。";
        noticeService.addSystemMessages(Set.of(user.getId()), "账号登录成功", content);
        return BeanConvertUtils.convert(authenticationSession.login(userInfo), LoginResultVO.class);
    }

    /**
     * 在认证结果最终确定后记录日志，失败尝试独立于业务事务记录。
     *
     * @param username 登录用户名
     * @param address 客户端地址
     * @param status 认证结果
     * @param remark 认证结果说明
     * @param credentialFailureUserId 凭据校验失败的已知账号，其他结果为空
     */
    private void publishLoginResult(String username, String address, LogStatus status, String remark, Long credentialFailureUserId) {
        if (status == LogStatus.FAILURE) {
            loginLogPublisher.publishLoginLog(username, address, status, remark, credentialFailureUserId);
            return;
        }
        // 只有动态码使用记录和登录事务确实提交后，才发布成功事件。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 提交成功后发布登录成功事件。
             */
            @Override
            public void afterCommit() {
                loginLogPublisher.publishLoginLog(username, address, status, remark);
            }
        });
    }

    /**
     * 根据 UUID 和客户端地址一次性核验验证码并返回失败原因。
     *
     * @param loginDTO 登录参数
     * @param clientAddress 本次登录的客户端地址
     * @return 校验失败原因，验证通过时为空
     */
    private String getCaptchaError(AuthLoginDTO loginDTO, String clientAddress) {
        // 提交时重新读取服务端开关，页面打开后重新启用验证码也必须校验。
        if (!configService.isLoginCaptchaEnabled()) {
            return null;
        }
        if (loginDTO.getUuid() == null || loginDTO.getCaptchaCode() == null) {
            return "请输入登录验证码";
        }
        String captchaKey = CAPTCHA_KEY_PREFIX + loginDTO.getUuid() + ":" + clientAddress;
        String expected = RedisUtils.takeCacheObject(captchaKey);
        if (expected == null) {
            return "验证码已过期，请刷新后重试";
        }
        return expected.equalsIgnoreCase(loginDTO.getCaptchaCode()) ? null : "验证码不正确，请重新输入";
    }

    /**
     * 构建用户、部门、岗位和角色授权快照，登录时一次性展开各角色的有效部门范围。
     *
     * @param user 登录用户
     * @return 包含部门、岗位和角色授权范围的登录信息
     */
    private UserInfo buildUserInfo(SysUser user) {
        UserInfo info = BeanConvertUtils.convert(user, UserInfo.class);
        info.setLoginType(AuthUserType.SYSTEM);
        // 超级管理员无需配置角色；其全部权限由可信账号身份统一放行。
        List<RoleInfo> roles = info.isSuperAdministrator() ? List.of() : roleService.getRoleInfoListByUserId(user.getId());
        info.setRoleInfoList(roles);
        info.setRoleCodeSet(roles.stream().map(RoleInfo::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        info.setPermissionRoleIds(permissionService.getPermissionRoleIds(roles));
        info.setPermissionCodeSet(new LinkedHashSet<>(info.getPermissionRoleIds().keySet()));
        info.setMenuIdSet(permissionService.getMenuIdsByRoleIds(roles.stream().map(RoleInfo::getRoleId)
                .collect(Collectors.toSet())));

        List<PostInfo> posts = postMapper.getPostInfoListByUserId(user.getId());
        Set<Long> departmentIds = new LinkedHashSet<>();
        if (user.getDeptId() != null) {
            departmentIds.add(user.getDeptId());
        }
        for (PostInfo post : posts) {
            if (post.getDeptId() != null) {
                departmentIds.add(post.getDeptId());
            }
        }
        Set<Long> customRoleIds = new LinkedHashSet<>();
        boolean includeDescendants = false;
        for (RoleInfo role : roles) {
            info.getRoleDepartmentIds().put(role.getRoleId(), new LinkedHashSet<>());
            switch (DataScopeType.fromCode(role.getDataScope())) {
                case CUSTOM_DEPARTMENTS -> customRoleIds.add(role.getRoleId());
                case DEPARTMENT_AND_DESCENDANTS -> includeDescendants = true;
                case ALL, OWN_DEPARTMENT, SELF -> {
                    // 这些范围不需要额外加载角色部门关系或下级部门。
                }
            }
        }
        if (!customRoleIds.isEmpty()) {
            for (SysRoleDept relation : userMapper.getRoleDepartmentsByRoleIds(customRoleIds)) {
                info.getRoleDepartmentIds().get(relation.getRoleId()).add(relation.getDeptId());
                departmentIds.add(relation.getDeptId());
            }
        }

        // 主部门、岗位部门和自定义范围共用一次祖先链校验。
        Map<Long, DepartmentInfo> departments = departmentIds.isEmpty() ? Map.of()
                : deptMapper.getValidDepartmentsByIds(departmentIds).stream()
                .collect(Collectors.toMap(DepartmentInfo::getDeptId, department -> department));
        info.setDepartmentInfo(user.getDeptId() == null ? null : departments.get(user.getDeptId()));
        info.setPostInfoList(posts.stream()
                .filter(post -> post.getDeptId() == null || departments.containsKey(post.getDeptId())).toList());
        Set<Long> descendantIds = includeDescendants && info.getDepartmentInfo() != null
                ? deptMapper.getValidDepartmentAndDescendantIds(user.getDeptId()) : Set.of();
        for (RoleInfo role : roles) {
            Set<Long> roleDepartments = info.getRoleDepartmentIds().get(role.getRoleId());
            switch (DataScopeType.fromCode(role.getDataScope())) {
                case CUSTOM_DEPARTMENTS -> roleDepartments.retainAll(departments.keySet());
                case OWN_DEPARTMENT -> {
                    if (info.getDepartmentInfo() != null) {
                        roleDepartments.add(user.getDeptId());
                    }
                }
                case DEPARTMENT_AND_DESCENDANTS -> roleDepartments.addAll(descendantIds);
                case ALL, SELF -> {
                    // 全部及本人范围由角色范围类型表达，不依赖部门集合。
                }
            }
        }
        return info;
    }
}
