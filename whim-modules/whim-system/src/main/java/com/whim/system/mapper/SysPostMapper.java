package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统岗位表数据库访问层
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
}

