package com.whim.mybatisplus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 声明适合由 SQL 拦截器追加租户条件的业务表。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "whim.persistence.tenant")
public class TenantIsolationProperties {
    /** 仅包含独立租户业务表；混合表和认证引导表由业务层显式处理。 */
    private Set<String> protectedTables = new HashSet<>();
}
