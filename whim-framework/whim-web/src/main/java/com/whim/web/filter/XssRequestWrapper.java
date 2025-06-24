package com.whim.web.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jince
 * date: 2025/6/24 15:11
 * description: xss过滤器
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value == null ? null : escapeXss(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        return Arrays.stream(values).map(this::escapeXss).toArray(String[]::new);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> escapedMap = new LinkedHashMap<>();
        originalMap.forEach((k, v) -> escapedMap.put(k, escapeXss(v)));
        return escapedMap;
    }

    private String[] escapeXss(String[] values) {
        return Arrays.stream(values).map(this::escapeXss).toArray(String[]::new);
    }

    private String escapeXss(String input) {
        // 使用 Spring 内置的 HTML 转义
        return HtmlUtils.htmlEscape(input.trim());
    }
}
