package com.whim.system.model.dto.notice;

import com.whim.system.model.entity.SysNotice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.AccessLevel;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 修改通知公告参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysNotice.class, reverseConvertGenerate = false)
public class SysNoticeUpdateDTO extends SysNoticeAddDTO {

    @NotNull(message = "公告ID不能为空")
    private Long id;
    @NotNull(message = "公告版本不能为空")
    @Min(value = 0, message = "公告版本无效")
    private Integer version;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private boolean publishTimeSpecified;

    /**
     * 区分显式清空发布时间与旧客户端未提交发布时间。
     *
     * @param publishTime 指定的发布时间，传空表示取消定时
     */
    @Override
    @JsonSetter("publishTime")
    public void setPublishTime(LocalDateTime publishTime) {
        super.setPublishTime(publishTime);
        publishTimeSpecified = true;
    }
}
