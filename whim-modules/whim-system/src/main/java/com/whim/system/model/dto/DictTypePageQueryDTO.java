package com.whim.system.model.dto;

import com.whim.mybatis.page.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jince
 * date: 2025/6/27 17:47
 * description: 字典类型分页查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictTypePageQueryDTO extends PageQueryDTO {
    private String name;
    private String type;
}
