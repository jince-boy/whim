package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/08
 * @description SysFile 持久化映射。
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
}
