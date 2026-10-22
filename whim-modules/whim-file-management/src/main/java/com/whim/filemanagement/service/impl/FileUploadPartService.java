package com.whim.filemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.filemanagement.mapper.FileUploadPartMapper;
import com.whim.filemanagement.model.entity.FileUploadPart;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 通过 IService 管理分片约束，事务边界由上传任务服务统一控制。
 */
@Service
public class FileUploadPartService extends ServiceImpl<FileUploadPartMapper, FileUploadPart> {
}

