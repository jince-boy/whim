package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysNoticeRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 公告首次阅读记录持久化。
 */
@Mapper
public interface SysNoticeReadMapper extends BaseMapper<SysNoticeRead> {
    /**
     * 创建阅读记录或推进已有记录的已读版本。
     *
     * @param receipts 包含本次所读消息版本的阅读记录
     */
    void updateReadReceipts(@Param("receipts") List<SysNoticeRead> receipts);
}
