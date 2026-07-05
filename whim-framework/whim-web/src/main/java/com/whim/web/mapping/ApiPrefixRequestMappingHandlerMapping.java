package com.whim.web.mapping;

import com.whim.web.annotation.ApiPrefix;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @author Jince
 * @date 2026/07/05
 * @description 根据控制器上的接口前缀注解动态追加请求路径前缀。
 */
public class ApiPrefixRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * 创建方法请求映射，并在控制器存在接口前缀注解时追加路径前缀。
     *
     * @param method 控制器方法
     * @param handlerType 控制器类型
     * @return 请求映射信息
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo mappingInfo = super.getMappingForMethod(method, handlerType);
        String prefix = findApiPrefix(handlerType);
        if (mappingInfo == null || !StringUtils.hasText(prefix)) {
            return mappingInfo;
        }
        RequestMappingInfo prefixInfo = RequestMappingInfo.paths(prefix)
                .options(getBuilderConfiguration())
                .build();
        return prefixInfo.combine(mappingInfo);
    }

    /**
     * 查找控制器类型上的接口路径前缀。
     *
     * @param handlerType 控制器类型
     * @return 接口路径前缀
     */
    private String findApiPrefix(Class<?> handlerType) {
        MergedAnnotation<ApiPrefix> apiPrefix = MergedAnnotations.from(handlerType, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ApiPrefix.class);
        if (!apiPrefix.isPresent()) {
            return null;
        }
        return normalizePrefix(apiPrefix.getString("path"));
    }

    /**
     * 规范化接口路径前缀。
     *
     * @param prefix 原始接口路径前缀
     * @return 规范化后的接口路径前缀
     */
    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return null;
        }
        String normalizedPrefix = prefix.trim();
        if (!normalizedPrefix.startsWith("/")) {
            normalizedPrefix = "/" + normalizedPrefix;
        }
        if (normalizedPrefix.length() > 1 && normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        return normalizedPrefix;
    }
}
