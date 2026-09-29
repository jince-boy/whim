package com.whim.system.model.vo.post;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 当前租户岗位响应。
 */
@Data
public class PostVO {
    private Long id;
    private Long deptId;
    private String postName;
    private String postCode;
    private Integer sort;
    private Integer status;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
}
