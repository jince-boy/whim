package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.config.properties.AltchaProperties;
import com.whim.core.exception.ServiceException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;
import com.whim.system.model.vo.auth.AuthTenantVO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.altcha.altcha.v2.Altcha;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    /**
     * ALTCHA 验证码配置
     */
    private final AltchaProperties altchaProperties;

    /**
     * 系统用户服务对象
     */
    private final ISysUserService sysUserService;

    /**
     * 系统角色服务对象
     */
    private final ISysRoleService sysRoleService;

    /**
     * 系统租户服务对象
     */
    private final ISysTenantService sysTenantService;

    /**
     * 用户租户关系服务对象
     */
    private final ISysUserTenantService sysUserTenantService;

    /**
     * 当前请求认证上下文
     */
    private final AuthenticationContext authenticationContext;

    /**
     * 认证会话操作对象
     */
    private final AuthenticationSession authenticationSession;

    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public AuthLoginVO login(AuthLoginDTO loginDTO) {
        verifyCaptcha(loginDTO.getAltcha());

        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
        if (user == null || !BCryptUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new UserPasswordNotMatchException("用户名不存在或密码错误");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }

        AuthenticationToken authenticationToken = authenticationSession.login(
                sysUserService.buildUserInfo(user),
                Boolean.TRUE.equals(loginDTO.getRememberMe())
        );
        return buildLoginVO(authenticationToken);
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        authenticationSession.logout();
    }

    /**
     * 获取当前用户信息并按指定或默认租户刷新当前令牌上下文。
     *
     * @param tenantId 目标租户ID，不传时使用默认租户
     * @return 当前登录用户信息
     */
    @Override
    public AuthUserVO getUserInfo(Long tenantId) {
        SysUser user = sysUserService.getById(authenticationContext.getUserId());
        if (user == null) {
            throw new UserNotFoundException("当前登录用户不存在");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }

        UserInfo userInfo = tenantId == null
                ? sysUserService.buildUserInfo(user)
                : sysUserService.buildUserInfo(user, tenantId);
        List<SysTenant> tenantList = sysRoleService.isSuperAdministrator(user.getId())
                ? sysTenantService.getAvailableTenantList()
                : sysUserTenantService.getTenantListByUserId(user.getId());
        AuthUserVO userVO = BeanConvertUtils.convert(userInfo, AuthUserVO.class);
        userVO.setTenantList(BeanConvertUtils.convertList(tenantList, AuthTenantVO.class));
        authenticationSession.updateUserInfo(userInfo);
        return userVO;
    }

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    @Override
    public AltchaCaptchaVO getCaptcha() {
        try {
            Altcha.CreateChallengeOptions options = new Altcha.CreateChallengeOptions()
                    .algorithm(altchaProperties.getAlgorithm())
                    .cost(altchaProperties.getCost())
                    .hmacSignatureSecret(altchaProperties.getHmacSignatureSecret())
                    .expiresInSeconds(altchaProperties.getExpiresInSeconds());
            Altcha.Challenge challenge = Altcha.createChallenge(options);

            AltchaCaptchaVO captcha = new AltchaCaptchaVO();
            captcha.setParameters(buildChallengeParameters(challenge.parameters()));
            captcha.setSignature(challenge.signature());
            return captcha;
        } catch (Exception exception) {
            log.warn("生成 ALTCHA 验证码失败", exception);
            throw new ServiceException("生成验证码失败", exception);
        }
    }

    /**
     * 构建 ALTCHA v2 验证码挑战参数，避免向前端输出空字段。
     *
     * @param parameters ALTCHA 挑战参数
     * @return 验证码挑战参数
     */
    private Map<String, Object> buildChallengeParameters(Altcha.ChallengeParameters parameters) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("algorithm", parameters.algorithm());
        result.put("cost", parameters.cost());
        if (parameters.data() != null) {
            result.put("data", parameters.data());
        }
        if (parameters.expiresAt() != null) {
            result.put("expiresAt", parameters.expiresAt());
        }
        result.put("keyLength", parameters.keyLength());
        result.put("keyPrefix", parameters.keyPrefix());
        if (parameters.keySignature() != null) {
            result.put("keySignature", parameters.keySignature());
        }
        if (parameters.memoryCost() != null) {
            result.put("memoryCost", parameters.memoryCost());
        }
        result.put("nonce", parameters.nonce());
        if (parameters.parallelism() != null) {
            result.put("parallelism", parameters.parallelism());
        }
        result.put("salt", parameters.salt());
        return result;
    }

    /**
     * 校验 ALTCHA 验证码答案。
     *
     * @param altcha 验证码答案
     */
    private void verifyCaptcha(String altcha) {
        Altcha.VerifySolutionResult result;
        try {
            result = Altcha.verifySolution(
                    altcha,
                    altchaProperties.getHmacSignatureSecret(),
                    Altcha.kdf(altchaProperties.getAlgorithm()));
        } catch (Exception exception) {
            throw new ServiceException("验证码校验失败", exception);
        }
        if (!result.verified()) {
            log.warn("验证码校验失败，expired={}, invalidSignature={}, invalidSolution={}",
                    result.expired(), result.invalidSignature(), result.invalidSolution());
            throw new ServiceException("验证码校验失败");
        }
    }

    /**
     * 构建登录响应参数。
     *
     * @param authenticationToken 登录令牌信息
     * @return 登录响应参数
     */
    private AuthLoginVO buildLoginVO(AuthenticationToken authenticationToken) {
        AuthLoginVO loginVO = new AuthLoginVO();
        loginVO.setPrefix(authenticationToken.getTokenType());
        loginVO.setToken(authenticationToken.getAccessToken());
        loginVO.setExpires(authenticationToken.getExpiresIn());
        return loginVO;
    }
}
