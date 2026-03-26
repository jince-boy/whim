package com.whim.core.utils;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/3/24
 * @description Servlet 请求工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServletUtils {

    private static final Logger log = LoggerFactory.getLogger(ServletUtils.class);

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String UNKNOWN = "Unknown";
    private static final Set<String> MOBILE_DEVICE_CLASSES = Set.of(
            "Mobile",
            "Tablet",
            "Phone",
            "Watch",
            "Augmented Reality",
            "Virtual Reality",
            "eReader"
    );

    /**
     * 获取当前线程请求属性。
     *
     * @return 请求属性
     */
    public static Optional<ServletRequestAttributes> currentRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return Optional.of(servletRequestAttributes);
        }
        return Optional.empty();
    }

    /**
     * 获取当前线程请求属性。
     *
     * @return 请求属性
     */
    public static ServletRequestAttributes getRequestAttributes() {
        return currentRequestAttributes().orElse(null);
    }

    /**
     * 获取当前线程请求对象。
     *
     * @return 请求对象
     */
    public static Optional<HttpServletRequest> currentRequest() {
        return currentRequestAttributes().map(ServletRequestAttributes::getRequest);
    }

    /**
     * 获取当前线程请求对象。
     *
     * @return 请求对象
     */
    public static HttpServletRequest getRequest() {
        return currentRequest().orElse(null);
    }

    /**
     * 获取当前请求语言环境。
     *
     * @return 语言环境
     */
    public static Locale getLocale() {
        return currentRequest()
                .map(HttpServletRequest::getLocale)
                .orElse(DEFAULT_LOCALE);
    }

    /**
     * 获取当前请求语言代码。
     *
     * @return 语言代码
     */
    public static String getLanguage() {
        return getLocale().getLanguage();
    }

    /**
     * 获取当前请求语言标签。
     *
     * @return 语言标签
     */
    public static String getLanguageTag() {
        return getLocale().toLanguageTag();
    }

    /**
     * 获取请求头。
     *
     * @param name 请求头名称
     * @return 请求头值
     */
    public static Optional<String> getHeader(String name) {
        return currentRequest()
                .map(request -> request.getHeader(name))
                .filter(value -> value != null && !value.isBlank());
    }

    /**
     * 获取请求参数。
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static String getParameter(String name) {
        return getParameterOptional(name).orElse(null);
    }

    /**
     * 获取请求参数。
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static Optional<String> getParameterOptional(String name) {
        return currentRequest()
                .map(request -> request.getParameter(name))
                .filter(value -> value != null && !value.isBlank());
    }

    /**
     * 获取请求参数。
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getParameter(String name, String defaultValue) {
        return getParameterOptional(name).orElse(defaultValue);
    }

    /**
     * 获取所有请求参数。
     *
     * @return 参数集合
     */
    public static Map<String, String> getAllParameters() {
        return currentRequest()
                .map(ServletUtils::getAllParameters)
                .orElseGet(Map::of);
    }

    /**
     * 获取所有请求参数。
     *
     * @param request 请求对象
     * @return 参数集合
     */
    public static Map<String, String> getAllParameters(ServletRequest request) {
        if (request == null || request.getParameterMap().isEmpty()) {
            return Map.of();
        }
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));
    }

    /**
     * 获取所有请求参数。
     *
     * @return 参数集合
     */
    public static Map<String, String> getParameterAll() {
        return getAllParameters();
    }

    /**
     * 获取所有请求参数。
     *
     * @param request 请求对象
     * @return 参数集合
     */
    public static Map<String, String> getParameterAll(ServletRequest request) {
        return getAllParameters(request);
    }

    /**
     * 获取整型请求参数。
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static Integer getParameterToInt(String name) {
        return ValueParserUtils.toInteger(getParameter(name));
    }

    /**
     * 获取整型请求参数。
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ValueParserUtils.toInteger(getParameter(name), defaultValue);
    }

    /**
     * 获取布尔请求参数。
     *
     * @param name 参数名称
     * @return 参数值
     */
    public static Boolean getParameterToBoolean(String name) {
        return ValueParserUtils.toBoolean(getParameter(name));
    }

    /**
     * 获取布尔请求参数。
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Boolean getParameterToBoolean(String name, Boolean defaultValue) {
        return ValueParserUtils.toBoolean(getParameter(name), defaultValue);
    }

    /**
     * 获取分页页码。
     *
     * @return 页码
     */
    public static Integer getPageNum() {
        return getParameterToInt("pageNum", DEFAULT_PAGE_NUM);
    }

    /**
     * 获取分页页码。
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 页码
     */
    public static Integer getPageNum(String name, Integer defaultValue) {
        return getParameterToInt(name, defaultValue);
    }

    /**
     * 获取分页大小。
     *
     * @return 分页大小
     */
    public static Integer getPageSize() {
        return getParameterToInt("pageSize", DEFAULT_PAGE_SIZE);
    }

    /**
     * 获取分页大小。
     *
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 分页大小
     */
    public static Integer getPageSize(String name, Integer defaultValue) {
        return getParameterToInt(name, defaultValue);
    }

    /**
     * 编码 URL 参数。
     *
     * @param value 参数值
     * @return 编码结果
     */
    public static String encodeUrlParam(String value) {
        return encodeUrlParam(value, StandardCharsets.UTF_8);
    }

    /**
     * 编码 URL 参数。
     *
     * @param value 参数值
     * @param charset 字符集
     * @return 编码结果
     */
    public static String encodeUrlParam(String value, Charset charset) {
        return encodeOrDecodeUrlParam(value, charset, true);
    }

    /**
     * 解码 URL 参数。
     *
     * @param value 参数值
     * @return 解码结果
     */
    public static String decodeUrlParam(String value) {
        return decodeUrlParam(value, StandardCharsets.UTF_8);
    }

    /**
     * 解码 URL 参数。
     *
     * @param value 参数值
     * @param charset 字符集
     * @return 解码结果
     */
    public static String decodeUrlParam(String value, Charset charset) {
        return encodeOrDecodeUrlParam(value, charset, false);
    }

    /**
     * 编码 URL 参数。
     *
     * @param value 参数值
     * @return 编码结果
     */
    public static String URLParamEncode(String value) {
        return encodeUrlParam(value);
    }

    /**
     * 编码 URL 参数。
     *
     * @param value 参数值
     * @param charset 字符集
     * @return 编码结果
     */
    public static String URLParamEncode(String value, Charset charset) {
        return encodeUrlParam(value, charset);
    }

    /**
     * 解码 URL 参数。
     *
     * @param value 参数值
     * @return 解码结果
     */
    public static String URLParamDecode(String value) {
        return decodeUrlParam(value);
    }

    /**
     * 解码 URL 参数。
     *
     * @param value 参数值
     * @param charset 字符集
     * @return 解码结果
     */
    public static String URLParamDecode(String value, Charset charset) {
        return decodeUrlParam(value, charset);
    }

    /**
     * 获取当前请求完整 URL。
     *
     * @return 完整请求 URL
     */
    public static String getRequestURL() {
        return currentRequest()
                .map(request -> request.getRequestURL().toString())
                .orElse(null);
    }

    /**
     * 获取当前请求来源地址。
     *
     * @return 请求来源地址
     */
    public static String getRequestOrigin() {
        return currentRequest()
                .map(ServletUtils::buildRequestOrigin)
                .orElse(null);
    }

    /**
     * 获取当前请求 User-Agent。
     *
     * @return User-Agent 原始值
     */
    public static Optional<String> getUserAgentHeader() {
        return getHeader("User-Agent");
    }

    /**
     * 解析当前请求 User-Agent。
     *
     * @return Yauaa 解析结果
     */
    public static UserAgent getUserAgent() {
        return getUserAgentHeader()
                .map(ServletUtils::parseUserAgent)
                .orElse(null);
    }

    /**
     * 解析指定 User-Agent。
     *
     * @param userAgent User-Agent 原始值
     * @return Yauaa 解析结果
     */
    public static UserAgent parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return UserAgentAnalyzerHolder.ANALYZER.parse(userAgent);
    }

    /**
     * 解析当前请求客户端信息。
     *
     * @return 客户端信息
     */
    public static ClientAgentInfo getClientAgentInfo() {
        return getUserAgentHeader()
                .map(ServletUtils::parseClientAgent)
                .orElseGet(ClientAgentInfo::empty);
    }

    /**
     * 解析请求客户端信息。
     *
     * @param request 请求对象
     * @return 客户端信息
     */
    public static ClientAgentInfo parseClientAgent(HttpServletRequest request) {
        if (request == null) {
            return ClientAgentInfo.empty();
        }
        return parseClientAgent(request.getHeader("User-Agent"));
    }

    /**
     * 解析 User-Agent 客户端信息。
     *
     * @param userAgent User-Agent 原始值
     * @return 客户端信息
     */
    public static ClientAgentInfo parseClientAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return ClientAgentInfo.empty();
        }
        try {
            UserAgent parsedUserAgent = parseUserAgent(userAgent);
            return parsedUserAgent == null
                    ? ClientAgentInfo.unknown(userAgent)
                    : ClientAgentInfo.from(userAgent, parsedUserAgent);
        } catch (RuntimeException exception) {
            log.warn("解析 User-Agent 失败: {}", userAgent, exception);
            return ClientAgentInfo.unknown(userAgent);
        }
    }

    private static String encodeOrDecodeUrlParam(String value, Charset charset, boolean encode) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return encode ? URLEncoder.encode(value, charset) : URLDecoder.decode(value, charset);
        } catch (RuntimeException exception) {
            log.warn("URL 参数处理失败: {}", value, exception);
            return value;
        }
    }

    private static String buildRequestOrigin(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder()
                .append(request.getScheme())
                .append("://")
                .append(request.getServerName());
        if (!isDefaultPort(request.getScheme(), request.getServerPort())) {
            builder.append(':').append(request.getServerPort());
        }
        return builder.toString();
    }

    private static boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
    }

    private static String normalizedUserAgentValue(UserAgent userAgent, String fieldName) {
        String value = userAgent.getValue(fieldName);
        if (value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private static boolean isMobileDevice(String deviceClass) {
        return deviceClass != null && MOBILE_DEVICE_CLASSES.contains(deviceClass);
    }

    private static boolean isBot(String deviceClass, String agentClass) {
        return (deviceClass != null && deviceClass.startsWith("Robot"))
                || (agentClass != null && agentClass.startsWith("Robot"));
    }

    private static String extractVersion(String agentName, String agentNameVersionMajor) {
        if (agentName == null || agentNameVersionMajor == null) {
            return null;
        }
        String version = agentNameVersionMajor.startsWith(agentName)
                ? agentNameVersionMajor.substring(agentName.length()).trim()
                : agentNameVersionMajor;
        return version.isBlank() ? null : version;
    }

    private static final class UserAgentAnalyzerHolder {

        private static final UserAgentAnalyzer ANALYZER = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(5_000)
                .withField("AgentName")
                .withField("AgentNameVersionMajor")
                .withField("AgentClass")
                .withField("DeviceClass")
                .withField("DeviceBrand")
                .withField("OperatingSystemNameVersionMajor")
                .withField("OperatingSystemClass")
                .withField("LayoutEngineNameVersionMajor")
                .build();

        private UserAgentAnalyzerHolder() {
        }
    }

    /**
     * 客户端信息。
     *
     * @param rawUserAgent 原始 User-Agent
     * @param browser 浏览器名称
     * @param browserVersion 浏览器版本
     * @param browserClass 浏览器类别
     * @param deviceClass 设备类别
     * @param deviceBrand 设备品牌
     * @param operatingSystem 操作系统
     * @param operatingSystemClass 操作系统类别
     * @param layoutEngine 渲染引擎
     * @param mobile 是否移动端
     * @param bot 是否机器人
     */
    public record ClientAgentInfo(
            String rawUserAgent,
            String browser,
            String browserVersion,
            String browserClass,
            String deviceClass,
            String deviceBrand,
            String operatingSystem,
            String operatingSystemClass,
            String layoutEngine,
            boolean mobile,
            boolean bot
    ) {

        private static ClientAgentInfo from(String rawUserAgent, UserAgent userAgent) {
            String browser = normalizedUserAgentValue(userAgent, "AgentName");
            String browserNameVersionMajor = normalizedUserAgentValue(userAgent, "AgentNameVersionMajor");
            String browserClass = normalizedUserAgentValue(userAgent, "AgentClass");
            String deviceClass = normalizedUserAgentValue(userAgent, "DeviceClass");
            String deviceBrand = normalizedUserAgentValue(userAgent, "DeviceBrand");
            String operatingSystem = normalizedUserAgentValue(userAgent, "OperatingSystemNameVersionMajor");
            String operatingSystemClass = normalizedUserAgentValue(userAgent, "OperatingSystemClass");
            String layoutEngine = normalizedUserAgentValue(userAgent, "LayoutEngineNameVersionMajor");

            return new ClientAgentInfo(
                    rawUserAgent,
                    browser,
                    extractVersion(browser, browserNameVersionMajor),
                    browserClass,
                    deviceClass,
                    deviceBrand,
                    operatingSystem,
                    operatingSystemClass,
                    layoutEngine,
                    isMobileDevice(deviceClass),
                    isBot(deviceClass, browserClass)
            );
        }

        /**
         * 创建空客户端信息。
         *
         * @return 空客户端信息
         */
        public static ClientAgentInfo empty() {
            return new ClientAgentInfo(null, null, null, null, null, null, null, null, null, false, false);
        }

        /**
         * 创建未知客户端信息。
         *
         * @param rawUserAgent 原始 User-Agent
         * @return 未知客户端信息
         */
        public static ClientAgentInfo unknown(String rawUserAgent) {
            return new ClientAgentInfo(rawUserAgent, null, null, null, null, null, null, null, null, false, false);
        }
    }
}
