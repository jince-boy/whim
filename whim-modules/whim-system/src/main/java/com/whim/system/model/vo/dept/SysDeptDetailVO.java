package com.whim.system.model.vo.dept;

import com.whim.system.model.entity.SysDept;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门修改表单使用的详情。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDept.class, convertGenerate = false)
public class SysDeptDetailVO extends SysDeptVO {
    /** 祖先部门ID路径。 */
    private String ancestors;

    /** 当前有效负责人的姓名。 */
    private String leaderName;

    /** 当前有效负责人的用户名。 */
    private String leaderUsername;

    /** 修改时间。 */
    private LocalDateTime updateTime;
}
