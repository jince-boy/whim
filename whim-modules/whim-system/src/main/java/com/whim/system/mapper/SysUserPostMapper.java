package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统用户岗位关联数据库访问层。
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
}

