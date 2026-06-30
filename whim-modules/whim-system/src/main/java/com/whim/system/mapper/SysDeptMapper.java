package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 部门表数据库访问层
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
}

