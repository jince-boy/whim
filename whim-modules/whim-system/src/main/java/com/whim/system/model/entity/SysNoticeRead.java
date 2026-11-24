package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 记录用户已读的最新公告版本，创建时间保留首次阅读时间。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeRead extends BaseEntity {

    private Long id;
    private Long noticeId;
    private Long userId;
    private Integer readVersion;
    @TableLogic
    private Long deleted;
}
