package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserPostMapper;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.service.ISysUserPostService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 用户岗位关联持久化实现。
 */
@Service
public class SysUserPostServiceImpl extends ServiceImpl<SysUserPostMapper, SysUserPost>
        implements ISysUserPostService {
}
