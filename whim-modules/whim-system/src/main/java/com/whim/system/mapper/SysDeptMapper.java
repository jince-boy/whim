package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表(SysDept)表数据库访问层
 *
 * @author Jince
 * @since 2025-06-27 16:32:54
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

}

