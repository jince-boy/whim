package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.service.ISysRoleDeptService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 复用框架维护角色自定义部门关联。
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptService {
}
