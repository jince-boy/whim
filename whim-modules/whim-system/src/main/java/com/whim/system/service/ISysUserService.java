package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.satoken.service.IAuthQueryService;
import com.whim.system.model.entity.SysUser;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 系统用户表服务接口
 */
public interface ISysUserService extends IService<SysUser>, IAuthQueryService {
}
