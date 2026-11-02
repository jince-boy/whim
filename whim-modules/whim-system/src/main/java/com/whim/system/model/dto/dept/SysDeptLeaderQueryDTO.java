package com.whim.system.model.dto.dept;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门负责人候选用户查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptLeaderQueryDTO extends PageQueryDTO {
    /** 用户姓名或用户名关键字。 */
    @Size(max = 64, message = "搜索关键字不能超过64个字符")
    private String keyword;
}
