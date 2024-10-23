package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserMapper;
import com.whim.model.entity.SysUser;
import com.whim.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}

