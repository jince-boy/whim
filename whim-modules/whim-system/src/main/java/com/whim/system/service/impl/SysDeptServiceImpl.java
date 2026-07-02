package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.entity.SysDept;
import com.whim.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统部门表服务实现类
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
}

