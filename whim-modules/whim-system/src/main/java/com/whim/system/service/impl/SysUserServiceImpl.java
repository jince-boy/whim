package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.ConvertUtils;
import com.whim.mybatis.annotation.DataColumn;
import com.whim.mybatis.annotation.DataPermission;
import com.whim.satoken.core.context.AuthContext;
import com.whim.satoken.core.logic.StpAuthManager;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.SysUserVO;
import com.whim.system.model.vo.UserInfoVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2024-10-23 19:45:05
 * @description 系统用户(SysUser)表服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final ISysPermissionService permissionService;

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

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo() {
        UserInfoVO userInfoVO = new UserInfoVO();
        Long userId = StpAuthManager.SYSTEM.getLoginIdAsLong();
        SysUser sysUser = this.getSysUserById(userId);
        SysUserVO sysUserVO = ConvertUtils.convert(sysUser, SysUserVO.class);
        userInfoVO.setUser(sysUserVO);
        userInfoVO.setPermissionCode(AuthContext.getUserInfo().getPermissionCodeSet());
        userInfoVO.setRoleCode(AuthContext.getUserInfo().getRoleCodeSet());
        userInfoVO.setMenus(permissionService.getMenuTreeByUserId(userId));
        return userInfoVO;
    }

    /**
     * 通过id查询用户
     *
     * @param id 用户id
     * @return SysUser
     */
    @Override
    public SysUser getSysUserById(Long id) {
        return this.getById(id);
    }

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id")
    })
    @Override
    public SysUser test() {
        return this.lambdaQuery().eq(SysUser::getUsername, "test").one();
    }
}

