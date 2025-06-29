package com.whim.system.model.vo;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/6/27 19:31
 * description:
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    private Long id;
    /**
     * 字典名称
     */
    private String name;
    /**
     * 字典类型(唯一标识)
     */
    private String type;
    /**
     * 状态(0-启用 1-禁用)
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
