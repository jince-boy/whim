package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysFileUploadTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 上传任务基础持久化映射。
 */
@Mapper
public interface SysFileUploadTaskMapper extends BaseMapper<SysFileUploadTask> {
}
