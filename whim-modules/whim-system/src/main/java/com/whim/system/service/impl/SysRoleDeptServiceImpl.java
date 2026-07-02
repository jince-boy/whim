package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.service.ISysRoleDeptService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色部门关联表服务实现类
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptService {
}

