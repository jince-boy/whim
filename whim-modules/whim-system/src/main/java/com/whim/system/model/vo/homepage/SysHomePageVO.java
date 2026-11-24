package com.whim.system.model.vo.homepage;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 首页管理列表与编辑信息。
 */
@Data
public class SysHomePageVO {
    private Long id;
    private Integer targetType;
    private Long targetId;
    private String targetName;
    private String path;
    private String component;
    private Integer priority;
    private Integer status;
}
