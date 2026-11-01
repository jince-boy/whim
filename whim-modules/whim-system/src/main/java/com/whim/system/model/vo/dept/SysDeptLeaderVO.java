package com.whim.system.model.vo.dept;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 本部门正常用户的负责人选项，不包含联系方式及认证数据。
 */
@Data
@AutoMapper(target = SysUser.class, convertGenerate = false)
public class SysDeptLeaderVO {
    /** 用户ID。 */
    private Long id;
    /** 用户名。 */
    private String username;
    /** 用户姓名。 */
    private String name;
}
