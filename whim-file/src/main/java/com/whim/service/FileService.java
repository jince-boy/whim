package com.whim.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.entity.SysFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 系统文件(SysFile)表服务接口
 *
 * @author JinCe
 * @since 2024-06-30 21:26:09
 */
public interface FileService extends IService<SysFile> {
    /**
     * 上传文件
     *
     * @param file        MultipartFile
     * @param folderName  自定义路径
     * @param description 文件描述
     * @return SysFile
     * @throws Exception ServiceException
     */
    SysFile uploadFile(MultipartFile file, String folderName, String description) throws Exception;

    /**
     * 删除文件
     *
     * @param fileId 文件id
     * @return true删除成功，false删除失败
     */
    boolean deleteFile(Long fileId) throws IOException;

    /**
     * 从本地存储中根据文件地址获取文件.
     *
     * @param filePath 文件地址
     * @return 代表文件的Resource
     */
    Resource getFile(String filePath) throws Exception;
}
