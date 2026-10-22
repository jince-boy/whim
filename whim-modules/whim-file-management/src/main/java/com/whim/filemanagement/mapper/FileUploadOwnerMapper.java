package com.whim.filemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.filemanagement.model.entity.FileUploadOwner;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 用户上传额度锁持久化映射。
 */
@Mapper
public interface FileUploadOwnerMapper extends BaseMapper<FileUploadOwner> {
}

