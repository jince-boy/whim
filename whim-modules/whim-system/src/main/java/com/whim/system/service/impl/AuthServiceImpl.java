package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.wf.captcha.GifCaptcha;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.core.auth.model.PostInfo;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.ServiceException;
import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.IdUtils;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.redis.utils.RedisUtils;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RateType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 登录认证及用户授权快照构建。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final Duration CAPTCHA_LIFETIME = Duration.ofMinutes(2);

    private static final String CAPTCHA_KEY_PREFIX = "auth:loginCaptcha:";

    /**
     * 系统用户服务对象
     */
    private final ISysUserService sysUserService;

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
        // 密码修改与管理员重置保持一致：事务成功后失效全部旧会话，回滚不影响登录。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /**
     * 生成 GIF 验证码，并按 UUID 和客户端地址保存答案。
     *
     * @param clientAddress 客户端地址
     * @return GIF 验证码
     */
    @Override
    public LoginCaptchaVO createCaptcha(String clientAddress) {
        if (RedisUtils.rateLimiter("auth:captchaIssue:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0) {
            throw new TooManyRequestsException("验证码获取过于频繁，请稍后重试");
        }
        GifCaptcha captcha = new GifCaptcha(160, 60, 4);
        String uuid = IdUtils.uuid();
        RedisUtils.setCacheObject(CAPTCHA_KEY_PREFIX + uuid + ":" + clientAddress, captcha.text(), CAPTCHA_LIFETIME);
        LoginCaptchaVO result = new LoginCaptchaVO();
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
    public AuthenticationToken login(AuthLoginDTO loginDTO) {
        String clientAddress = IPUtils.getClientIpAddress();
        if (RedisUtils.rateLimiter("auth:loginIp:" + clientAddress, RateType.OVERALL, 60, 60, 120) < 0
                || RedisUtils.rateLimiter("auth:loginUser:" + loginDTO.getUsername(), RateType.OVERALL, 10, 60, 120) < 0) {
            throw new TooManyRequestsException("登录请求过于频繁，请稍后重试");
        }
        // 验证验证码
        validateCaptcha(loginDTO);
        SysUser user = sysUserService.getByUsername(loginDTO.getUsername());
        if (user == null || !BCryptUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new UserPasswordNotMatchException("用户名不存在或密码错误");
        }
        if (SysUserStatus.DISABLED.matches(user.getStatus())) {
            throw new UserDisableException("用户已被禁用");
        }

        return authenticationSession.login(buildUserInfo(user));
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        authenticationSession.logout();
    }

    /**
     * 根据 UUID 和客户端地址一次性取出验证码，分别提示过期和答案错误。
     *
     * @param loginDTO 登录参数
     */
    private void validateCaptcha(AuthLoginDTO loginDTO) {
        String captchaKey = CAPTCHA_KEY_PREFIX + loginDTO.getUuid() + ":" + IPUtils.getClientIpAddress();
        String expected = RedisUtils.takeCacheObject(captchaKey);
        if (expected == null) {
            throw new IllegalArgumentException("验证码已过期，请刷新后重试");
        }
        if (!expected.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
            throw new IllegalArgumentException("验证码不正确，请重新输入");
        }
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
