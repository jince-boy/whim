package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 字典数据表实体类
 */
@Data
public class SysDictData implements Serializable {
    @Serial
    private static final long serialVersionUID = -89129477403064855L;

    /**
     * 字典数据ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 字典类型(关联sys_dict_type.type)
     */
    private String dictType;

    /**
     * 字典标签(显示文本)
     */
    private String label;

    /**
     * 字典键值(存储值)
     */
    private String value;

    /**
     * 回显样式类名(如: primary)
     */
    private String listClass;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}

