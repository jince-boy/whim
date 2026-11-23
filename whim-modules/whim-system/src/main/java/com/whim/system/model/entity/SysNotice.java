package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.whim.mybatisplus.model.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 消息中心记录，公开通知公告与个人系统消息按收件人区分。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {

    private Long id;
    private String title;
    private Integer noticeType;
    /** 仅系统自动生成的消息指定收件人，发布后归属不可变。 */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Long recipientUserId;
    private String content;
    /** 纯文本客户端改写正文时应一并清空旧排版。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String contentHtml;
    private Integer status;
    /** 读者可见的起始时间，关闭状态可清空已设定的时间。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime publishTime;
    @Version
    private Integer version;
    @TableLogic
    private Long deleted;
}
