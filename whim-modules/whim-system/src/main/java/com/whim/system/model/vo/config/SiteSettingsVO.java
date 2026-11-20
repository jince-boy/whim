package com.whim.system.model.vo.config;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 允许在登录前展示的网站信息，未配置的字段沿用部署默认值。
 */
@Data
public class SiteSettingsVO {
    private String title;
    private String description;
    private String copyright;
}
