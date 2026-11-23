package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysNoticeRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 公告首次阅读记录持久化。
 */
@Mapper
public interface SysNoticeReadMapper extends BaseMapper<SysNoticeRead> {
    /**
     * 并发打开同一公告时保留最早的阅读记录。
     *
     * @param receipt 首次阅读记录
     */
    void addFirstRead(SysNoticeRead receipt);
}
