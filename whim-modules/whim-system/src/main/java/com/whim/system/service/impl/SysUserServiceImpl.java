package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.mybatis.annotation.DataColumn;
import com.whim.mybatis.annotation.DataPermission;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * @description 系统用户(SysUser)表服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

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

    @DataPermission({
            @DataColumn(key = "deptName", value = "dept_id")
    })
    @Override
    public SysUser test() {
        return this.lambdaQuery().eq(SysUser::getUsername, "test").one();
    }
}

