package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 用户岗位关联持久化接口。
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
}
