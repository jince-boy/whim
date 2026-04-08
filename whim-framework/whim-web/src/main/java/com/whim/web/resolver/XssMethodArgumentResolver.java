package com.whim.web.resolver;

import com.whim.web.annotation.Xss;
import com.whim.web.xss.XssSanitizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Objects;

/**
 * Sanitizes controller string parameters annotated with {@link Xss}.
 */
public final class XssMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final XssSanitizer xssSanitizer;

    public XssMethodArgumentResolver(XssSanitizer xssSanitizer) {
        this.xssSanitizer = Objects.requireNonNull(xssSanitizer, "Parameter [xssSanitizer] must not be null");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return String.class == parameter.getParameterType() && parameter.hasParameterAnnotation(Xss.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        var xss = parameter.getParameterAnnotation(Xss.class);
        return xssSanitizer.sanitize(resolveRawValue(parameter, webRequest), xss);
    }

    private String resolveRawValue(MethodParameter parameter, NativeWebRequest webRequest) {
        String name = resolveParameterName(parameter);
        if (!StringUtils.hasText(name)) {
            return null;
        }

        var requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return webRequest.getParameter(name);
        }

        var pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return resolvePathVariable(webRequest, name);
        }

        var requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return webRequest.getHeader(name);
        }

        var cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null) {
            return resolveCookieValue(webRequest, name);
        }

        String requestValue = webRequest.getParameter(name);
        if (requestValue != null) {
            return requestValue;
        }
        return resolvePathVariable(webRequest, name);
    }

    private String resolveParameterName(MethodParameter parameter) {
        var requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return resolveName(requestParam.name(), requestParam.value(), parameter.getParameterName());
        }

        var pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return resolveName(pathVariable.name(), pathVariable.value(), parameter.getParameterName());
        }

        var requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return resolveName(requestHeader.name(), requestHeader.value(), parameter.getParameterName());
        }

        var cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null) {
            return resolveName(cookieValue.name(), cookieValue.value(), parameter.getParameterName());
        }

        return parameter.getParameterName();
    }

    private String resolveName(String primary, String secondary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        if (StringUtils.hasText(secondary)) {
            return secondary;
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private String resolvePathVariable(NativeWebRequest webRequest, String name) {
        Object attribute = webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (!(attribute instanceof Map<?, ?> variables)) {
            return null;
        }
        Object value = ((Map<String, Object>) variables).get(name);
        return value == null ? null : value.toString();
    }

    private String resolveCookieValue(NativeWebRequest webRequest, String name) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
