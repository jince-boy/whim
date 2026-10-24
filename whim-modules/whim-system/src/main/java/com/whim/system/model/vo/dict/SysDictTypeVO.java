package com.whim.system.model.vo.dict;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 字典类型响应，不暴露删除标识及操作者ID。
 */
@Data
@AutoMapper(target = SysDictType.class, convertGenerate = false)
public class SysDictTypeVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    private Long id;
    /** 字典名称。 */
    private String name;
    /** 字典类型编码。 */
    private String type;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
}
