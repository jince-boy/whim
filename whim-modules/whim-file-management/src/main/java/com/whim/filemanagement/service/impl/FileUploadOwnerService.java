package com.whim.filemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.filemanagement.mapper.FileUploadOwnerMapper;
import com.whim.filemanagement.model.entity.FileUploadOwner;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 使用 IService 维护用户上传额度锁。
 */
@Service
public class FileUploadOwnerService extends ServiceImpl<FileUploadOwnerMapper, FileUploadOwner> {
}

