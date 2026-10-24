package com.whim.system.model.vo.dict;

import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 字典数据响应，不暴露删除标识及操作者ID。
 */
@Data
@AutoMapper(target = SysDictData.class, convertGenerate = false)
public class SysDictDataVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    private Long id;
    /** 所属字典类型编码。 */
    private String type;
    /** 字典标签。 */
    private String label;
    /** 字典值。 */
    private String value;
    /** 回显样式。 */
    private String listClass;
    /** 排序值。 */
    private Integer sort;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
}
