package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.system.model.entity.SysNotice;
import com.whim.system.model.vo.notice.SysNoticeInboxItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 公告管理与当前用户收件列表查询。
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
    /**
     * 分页查询已发布的公开消息与当前账号的系统消息，并关联本人阅读状态。
     *
     * @param page 分页参数
     * @param userId 当前用户ID
     * @param noticeType 消息类型，为空时查询全部
     * @param now 本次查询的可见时间上限
     * @return 含阅读状态的消息页
     */
    Page<SysNoticeInboxItemVO> getInboxPage(Page<SysNoticeInboxItemVO> page, @Param("userId") Long userId,
                                            @Param("noticeType") Integer noticeType, @Param("now") LocalDateTime now);

    /**
     * 统计当前账号可见且尚未阅读的消息数。
     *
     * @param userId 当前用户ID
     * @param now 本次查询的可见时间上限
     * @return 未读总数
     */
    long getUnreadCount(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 按本人可见的最新消息划定范围，批量逻辑删除个人系统消息并填写删除审计字段。
     *
     * @param userId 当前登录账号ID
     * @param throughId 本次清空包含的最新系统消息ID
     * @param now 收件箱可见时间上限
     * @return 实际清空的消息条数
     */
    int clearSystemMessages(@Param("userId") Long userId, @Param("throughId") Long throughId,
                            @Param("now") LocalDateTime now);
}
