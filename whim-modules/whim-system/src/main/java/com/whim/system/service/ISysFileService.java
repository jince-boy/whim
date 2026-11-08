package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.dto.file.*;
import com.whim.system.model.vo.file.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理业务，使用组织级功能权限控制管理范围。
 */
public interface ISysFileService extends IService<SysFile> {
    /**
     * 分页查询文件。
     *
     * @param query 查询条件
     * @return 文件分页
     */
    PageDataVO<SysFileVO> getFilePage(SysFileQueryDTO query);

    /**
     * 查询存储配置列表，不返回密钥。
     *
     * @return 存储配置列表
     */
    List<SysFileStorageVO> getStorageList();

    /**
     * 查询文件上传限制。
     *
     * @return 单文件字节上限
     */
    Long getUploadLimit();

    /**
     * 使用系统默认上传配置保存文件和记录。
     *
     * @param parameter 上传参数
     * @return 文件记录
     */
    SysFileVO addFile(SysFileUploadDTO parameter);

    /**
     * 新增存储配置。
     *
     * @param parameter 配置参数
     */
    void addStorage(SysFileStorageAddDTO parameter);

    /**
     * 修改存储配置并保护已有文件定位。
     *
     * @param parameter 配置参数
     */
    void updateStorage(SysFileStorageUpdateDTO parameter);

    /**
     * 切换默认存储。
     *
     * @param id 配置ID
     */
    void updateDefaultStorage(Long id);

    /**
     * 删除文件及实际存储对象。
     *
     * @param id 文件ID
     */
    void deleteFile(Long id);

    /**
     * 删除未使用的非默认存储配置。
     *
     * @param id 配置ID
     */
    void deleteStorage(Long id);

    /**
     * 将文件安全下载到响应流。
     *
     * @param id 文件ID
     * @param response 下载响应
     */
    void downloadFile(Long id, HttpServletResponse response);

}
