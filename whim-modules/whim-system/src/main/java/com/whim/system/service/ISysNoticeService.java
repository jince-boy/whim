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

import java.util.Set;

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
     * 分页查询已发布的公共消息和当前账号收到的系统消息。
     *
     * @param query 分页条件
     * @return 含本人已读状态的消息分页
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
     * 查询本人收件箱中消息的当前版本，供已打开的详情检查变更。
     *
     * @param id 消息ID
     * @return 当前可见的版本，不可见时为空
     */
    Integer getInboxNoticeVersion(Long id);

    /**
     * 获取可访问公告中关联图片的 PNG 预览。
     *
     * @param query 公告和图片文件ID
     * @return 图片内容，不可见或不存在时为空数组
     */
    byte[] getNoticeImage(SysNoticeImageQueryDTO query);

    /**
     * 查询当前账号各分类的未读数、未读总数与所选分类的最近消息。
     *
     * @param noticeType 最近消息的类型，为空时查询全部
     * @return 消息中心摘要
     */
    SysNoticeBoxVO getNoticeBox(Integer noticeType);

    /**
     * 新增系统公告。
     *
     * @param parameter 标题、类型、正文与状态
     */
    void addNotice(SysNoticeAddDTO parameter);

    /**
     * 为本次业务变更涉及的账号保存个人系统消息。
     *
     * @param recipientIds 实际受影响的账号ID
     * @param title 消息标题
     * @param content 纯文本正文
     */
    void addSystemMessages(Set<Long> recipientIds, String title, String content);

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
     * 清空当前账号截至指定消息的已读和未读系统消息。
     *
     * @param throughId 打开确认框时看到的最新系统消息ID
     */
    void clearSystemMessages(Long throughId);

    /**
     * 读取本人可见的消息并记录本次返回的版本。
     *
     * @param id 公告ID
     * @return 消息详情，不可见或不存在时为空
     */
    SysNoticeDetailVO readNotice(Long id);

    /**
     * 将当前账号各分类中已发布且可见的未读消息全部标为已读。
     */
    void markAllNoticesRead();
}
