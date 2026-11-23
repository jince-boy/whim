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
     * 分页关联当前账号的阅读记录，只返回公开公告。
     *
     * @param page 分页参数
     * @param userId 当前用户ID
     * @param noticeType 消息类型，为空时查询全部
     * @param now 本次查询的可见时间上限
     * @return 含阅读状态的公告页
     */
    Page<SysNoticeInboxItemVO> getInboxPage(Page<SysNoticeInboxItemVO> page, @Param("userId") Long userId,
                                            @Param("noticeType") Integer noticeType, @Param("now") LocalDateTime now);

    /**
     * 统计当前用户可见且尚未阅读的公告数。
     *
     * @param userId 当前用户ID
     * @param now 本次查询的可见时间上限
     * @return 未读总数
     */
    long getUnreadCount(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
