package com.whim.core.utils;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * @date 2024/10/5 00:22
 * @description Servlet工具类
 */
@Slf4j
public class ServletUtils {

    /**
     * 获取请求属性
     *
     * @return ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取Request
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        log.error("获取ServletRequest异常");
        return null;
    }

    /**
     * 返回请求头中的语言
     *
     * @return 语言
     */
    public static String getLanguage() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getHeader("Accept-Language") : "en";
    }

    /**
     * 获取参数 params form-data x-www-form-urlencoded
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request != null ? request.getParameter(name) : null;
    }

    /**
     * 获取参数并赋予默认值 params form-data x-www-form-urlencoded
     *
     * @param name         参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(String name, String defaultValue) {
        return ConvertUtils.toString(getParameter(name), defaultValue);
    }

    /**
     * 获取所有参数
     *
     * @param request HttpServletRequest/ServletRequest
     * @return Map 参数集合
     */
    public static Map<String, String> getParameterAll(ServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            params.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return params;
    }

    /**
     * 获取参数并转换为int类型
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static Integer getParameterToInt(String name) {
        return ConvertUtils.toInt(getParameter(name));
    }

    /**
     * 获取参数并转换为int类型
     *
     * @param name         参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ConvertUtils.toInt(getParameter(name), defaultValue);
    }

    /**
     * 获取参数并转换为boolean类型
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static Boolean getParameterToBoolean(String name) {
        return ConvertUtils.toBoolean(getParameter(name));
    }

    /**
     * 获取参数并转换为boolean类型
     *
     * @param name         参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Boolean getParameterToBoolean(String name, Boolean defaultValue) {
        return ConvertUtils.toBoolean(getParameter(name), defaultValue);
    }

    /**
     * 获取分页参数
     *
     * @return 页码
     */
    public static Integer getPageNum() {
        return getParameterToInt("pageNum", 1);
    }

    /**
     * 获取分页参数
     *
     * @param name         页码
     * @param defaultValue 默认值
     * @return 页码
     */
    public static Integer getPageNum(String name, Integer defaultValue) {
        return getParameterToInt(name, defaultValue);
    }

    /**
     * 获取分页参数
     *
     * @return 页面大小
     */
    public static Integer getPageSize() {
        return getParameterToInt("pageSize", 10);
    }

    /**
     * 获取分页参数
     *
     * @param name         页面大小
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getPageSize(String name, Integer defaultValue) {
        return getParameterToInt(name, defaultValue);
    }

    /**
     * 将URL参数值进行编码或解码
     *
     * @param value    参数值
     * @param charset  字符集，用于编码或解码
     * @param isEncode 指示操作类型，true表示编码，false表示解码
     * @return 编码或解码后的参数值
     */
    private static String encodeOrDecodeURLParam(String value, Charset charset, boolean isEncode) {
        try {
            return isEncode ? URLEncoder.encode(value, charset) : URLDecoder.decode(value, charset);
        } catch (Exception e) {
            log.error("URL参数处理异常", e);
            return value; // 返回原始值以防止丢失数据
        }
    }

    /**
     * 将URL的参数值进行UTF-8编码
     *
     * @param value 参数值
     * @return 编码后的参数值
     */
    public static String URLParamEncode(String value) {
        return encodeOrDecodeURLParam(value, StandardCharsets.UTF_8, true);
    }

    /**
     * 将URL的参数值进行自定义字符集编码
     *
     * @param value  参数值
     * @param encode 自定义字符集，用于编码
     * @return 编码后的参数值
     */
    public static String URLParamEncode(String value, Charset encode) {
        return encodeOrDecodeURLParam(value, encode, true);
    }

    /**
     * 将URL编码后的参数值进行UTF-8解码
     *
     * @param value 编码后的参数值
     * @return 解码后的参数值
     */
    public static String URLParamDecode(String value) {
        return encodeOrDecodeURLParam(value, StandardCharsets.UTF_8, false);
    }

    /**
     * 将URL编码后的参数值进行自定义字符集解码
     *
     * @param value  编码后的参数值
     * @param encode 自定义字符集，用于解码
     * @return 解码后的参数值
     */
    public static String URLParamDecode(String value, Charset encode) {
        return encodeOrDecodeURLParam(value, encode, false);
    }

    /**
     * 获取请求地址
     *
     * @return 请求地址
     */
    public static String getRequestURL() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        }
        return null; // 或者返回一个默认值，例如 "http://localhost:8080"
    }

    /**
     * 获取User-Agent
     *
     * @return User-Agent
     */
    public static UserAgent getUserAgent() {
        HttpServletRequest request = getRequest();
        return UserAgent.parseUserAgentString(Objects.requireNonNull(request).getHeader("User-Agent"));
    }
}
