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
 * @description 文件业务统一入口，供文件管理及头像、附件等业务复用上传能力。
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
     * 查询文件的 PNG 缩略图，非图片或不可预览的文件返回空数组。
     *
     * @param id 文件ID
     * @return 最大96像素的等比 PNG 缩略图内容
     */
    byte[] getFileThumbnail(Long id);

    /**
     * 查询经过重新编码的安全图片预览，不返回原始上传内容。
     *
     * @param id 文件ID
     * @return 最大2048像素的PNG预览，不可预览时为空数组
     */
    byte[] getFilePreview(Long id);

    /**
     * 查询当前用户的上传任务及已确认的分片。
     *
     * @param id 上传任务ID
     * @return 任务进度
     */
    SysFileUploadTaskVO getFileUploadTask(Long id);

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
     * 使用系统默认上传配置保存文件及业务入口传入的上传途径。
     *
     * @param parameter 上传参数
     * @return 文件记录
     */
    SysFileVO addFile(SysFileUploadDTO parameter);

    /**
     * 使用默认配置创建可恢复上传任务。
     *
     * @param parameter 文件信息及幂等请求标识
     * @return 任务进度及分片大小
     */
    SysFileUploadTaskVO addFileUploadTask(SysFileUploadTaskAddDTO parameter);

    /**
     * 校验并提交一个文件分片。
     *
     * @param parameter 任务ID、序号及分片文件
     */
    void addFileUploadPart(SysFileUploadPartDTO parameter);

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
     * 清理过期任务及对应的临时文件。
     */
    void deleteExpiredFileUploads();

    /**
     * 将文件安全下载到响应流。
     *
     * @param id 文件ID
     * @param response 下载响应
     */
    void downloadFile(Long id, HttpServletResponse response);

    /**
     * 合并分片并原子保存文件记录和任务完成状态。
     *
     * @param id 上传任务ID
     * @return 已完成的任务
     */
    SysFileUploadTaskVO completeFileUpload(Long id);

    /**
     * 取消当前用户的任务并清理未完成内容。
     *
     * @param id 上传任务ID
     * @return 取消后的任务状态
     */
    SysFileUploadTaskVO cancelFileUpload(Long id);
}
