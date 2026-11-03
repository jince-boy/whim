package com.whim.system.model.vo.post;

import com.whim.system.model.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位列表及详情。
 */
@Data
@AutoMapper(target = SysPost.class, convertGenerate = false)
public class SysPostVO {
    /** 岗位ID。 */
    private Long id;
    /** 所属部门ID，null表示全组织岗位。 */
    private Long deptId;
    /** 岗位名称。 */
    private String postName;
    /** 岗位编码。 */
    private String postCode;
    /** 排序值。 */
    private Integer sort;
    /** 状态：0正常、1停用。 */
    private Integer status;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 修改时间。 */
    private LocalDateTime updateTime;
}
