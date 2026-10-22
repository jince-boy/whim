package com.whim.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.filemanagement.model.entity.FileUploadTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件上传任务持久化映射。
 */
@Mapper
public interface FileUploadTaskMapper extends BaseMapper<FileUploadTask> {
}

