package com.whim.system.model.vo.dept;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门负责人候选用户。
 */
@Data
@AutoMapper(target = SysUser.class, convertGenerate = false)
public class SysDeptLeaderVO {
    /** 用户ID。 */
    private Long id;

    /** 用户姓名。 */
    private String name;

    /** 用户名。 */
    private String username;
}
