package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 用户岗位关联表数据库访问层
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
}

