package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.service.ISysRoleDeptService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色与部门关联表服务实现类
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptService {
}

