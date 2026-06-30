package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统用户表服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
}

