package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表数据库访问层
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}

