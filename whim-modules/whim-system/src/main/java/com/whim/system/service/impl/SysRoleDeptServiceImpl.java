package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.service.ISysRoleDeptService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 角色自定义部门关联持久化实现。
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept>
        implements ISysRoleDeptService {
}
