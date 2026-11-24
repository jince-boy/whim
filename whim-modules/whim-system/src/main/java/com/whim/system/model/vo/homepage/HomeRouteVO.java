package com.whim.system.model.vo.homepage;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前用户按优先顺序匹配的首页路由。
 */
@Data
public class HomeRouteVO {
    private String path;
    private String component;
    private Long menuId;
}
