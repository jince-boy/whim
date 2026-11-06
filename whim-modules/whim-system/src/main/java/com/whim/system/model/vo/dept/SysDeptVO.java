package com.whim.system.model.vo.dept;

import com.whim.system.model.entity.SysDept;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门列表的树形节点。
 */
@Data
@AutoMapper(target = SysDept.class, convertGenerate = false)
public class SysDeptVO {
    /** 部门ID。 */
    private Long id;

    /** 上级部门ID，0表示根部门。 */
    private Long parentId;

    /** 部门名称。 */
    private String deptName;

    /** 部门编码。 */
    private String deptCode;

    /** 负责人用户ID。 */
    private Long leaderUserId;

    /** 联系电话。 */
    private String phone;

    /** 联系邮箱。 */
    private String email;

    /** 排序值。 */
    private Integer sort;

    /** 状态：0正常、1停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

    /** 创建时间，由持久化层自动填充。 */
    private LocalDateTime createTime;

    /** 数据权限范围内的下级部门，无下级时为空数组。 */
    private List<SysDeptVO> children = new ArrayList<>();
}
