package com.whim.system.model.dto.homepage;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页管理分页筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysHomePageQueryDTO extends PageQueryDTO {
    private Integer targetType;
    private Integer status;
}
