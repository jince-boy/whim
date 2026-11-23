package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.notice.SysNoticeInboxQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.notice.SysNoticeAddDTO;
import com.whim.system.model.dto.notice.SysNoticeImageQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeQueryDTO;
import com.whim.system.model.dto.notice.SysNoticeUpdateDTO;
import com.whim.system.model.entity.SysNotice;
import com.whim.system.model.vo.notice.SysNoticeBoxVO;
import com.whim.system.model.vo.notice.SysNoticeDetailVO;
import com.whim.system.model.vo.notice.SysNoticeInboxItemVO;
import com.whim.system.model.vo.notice.SysNoticeVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 系统公告管理和个人阅读服务。
 */
public interface ISysNoticeService extends IService<SysNotice> {
    /**
     * 分页查询管理端公告。
     *
     * @param query 筛选和分页条件
     * @return 公告摘要分页
     */
    PageDataVO<SysNoticeVO> getNoticePage(SysNoticeQueryDTO query);

    /**
     * 分页查询当前用户可以阅读的公告。
     *
     * @param query 分页条件
     * @return 含已读状态的公告分页
     */
    PageDataVO<SysNoticeInboxItemVO> getInboxPage(SysNoticeInboxQueryDTO query);

    /**
     * 查询管理端公告详情。
     *
     * @param id 公告ID
     * @return 公告详情，不存在时为空
     */
    SysNoticeDetailVO getNoticeDetail(Long id);

    /**
     * 获取可访问公告中关联图片的 PNG 预览。
     *
     * @param query 公告和图片文件ID
     * @return 图片内容，不可见或不存在时为空数组
     */
    byte[] getNoticeImage(SysNoticeImageQueryDTO query);

    /**
     * 查询当前用户未读数与最近公告。
     *
     * @param noticeType 最近消息的类型，为空时查询全部
     * @return 公告消息盒子
     */
    SysNoticeBoxVO getNoticeBox(Integer noticeType);

    /**
     * 新增系统公告。
     *
     * @param parameter 标题、类型、正文与状态
     */
    void addNotice(SysNoticeAddDTO parameter);

    /**
     * 将公告图片保存至系统默认文件存储。
     *
     * @param file 本地图片
     * @return 文件ID
     */
    Long addNoticeImage(MultipartFile file);

    /**
     * 按编辑版本更新系统公告。
     *
     * @param parameter 公告ID、原版本及新内容
     * @return 是否更新成功
     */
    boolean updateNotice(SysNoticeUpdateDTO parameter);

    /**
     * 逻辑删除系统公告。
     *
     * @param id 公告ID
     * @return 是否删除成功
     */
    boolean deleteNotice(Long id);

    /**
     * 读取可见公告并记录当前用户首次阅读。
     *
     * @param id 公告ID
     * @return 公告详情，已关闭或不存在时为空
     */
    SysNoticeDetailVO readNotice(Long id);
}
