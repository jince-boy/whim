package com.whim.system.model.vo.dept;

import com.whim.system.model.entity.SysDept;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门详情及树节点，不暴露逻辑删除和审计操作者字段。
 */
@Data
@AutoMapper(target = SysDept.class, convertGenerate = false)
public class SysDeptVO {
    /** 部门ID。 */
    private Long id;
    /** 上级部门ID。 */
    private Long parentId;
    /** 祖先部门路径。 */
    private String ancestors;
    /** 部门名称。 */
    private String deptName;
    /** 部门编码。 */
    private String deptCode;
    /** 负责人用户ID。 */
    private Long leaderUserId;
    /** 本部门正常负责人的姓名，未设置或失效时为空。 */
    private String leaderName;
    /** 本部门正常负责人的用户名，未设置或失效时为空。 */
    private String leaderUsername;
    /** 联系电话。 */
    private String phone;
    /** 邮箱。 */
    private String email;
    /** 排序值。 */
    private Integer sort;
    /** 状态：0正常、1停用。 */
    private Integer status;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 可见的下级部门，详情查询时为空。 */
    private List<SysDeptVO> children = new ArrayList<>();
}
