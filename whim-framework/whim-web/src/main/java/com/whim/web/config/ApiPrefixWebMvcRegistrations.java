package com.whim.web.config;

import com.whim.web.mapping.ApiPrefixRequestMappingHandlerMapping;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Jince
 * @date 2026/07/05
 * @description 注册支持接口前缀注解的 Spring MVC 组件。
 */
public class ApiPrefixWebMvcRegistrations implements WebMvcRegistrations {

    /**
     * 创建支持 {@code @ApiPrefix} 元注解的请求映射处理器。
     *
     * @return 请求映射处理器
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiPrefixRequestMappingHandlerMapping();
    }
}
