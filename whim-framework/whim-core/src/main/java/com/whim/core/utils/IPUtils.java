package com.whim.core.utils;

import com.whim.core.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/03/26
 * @description 基于 ip2region xdb 的 IP 工具类，适配 Spring Boot 4 与 JDK 25，支持 IPv4/IPv6 离线归属地查询与反向代理场景下的客户端 IP 提取。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IPUtils {

    /**
     * IPv4 数据库文件名。
     */
    public static final String IPV4_DATA_FILE = "ip2region_v4.xdb";

    /**
     * IPv6 数据库文件名。
     */
    public static final String IPV6_DATA_FILE = "ip2region_v6.xdb";

    private static final Logger log = LoggerFactory.getLogger(IPUtils.class);

    private static final String UNKNOWN = "unknown";
    private static final String FORWARDED_HEADER = "Forwarded";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final List<String> CLIENT_IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP"
    );

    /**
     * 查询当前请求客户端的归属地信息。
     *
     * @return 归属地信息，无法识别时返回 {@code null}
     */
    public static String getCityInfo() {
        return getCityInfo(getClientIpAddress());
    }

    /**
     * 查询指定 IP 的归属地信息。
     *
     * @param ip IP 地址
     * @return 归属地信息，无法识别时返回 {@code null}
     */
    public static String getCityInfo(String ip) {
        return getCityInfoOptional(ip).orElse(null);
    }

    /**
     * 查询当前请求客户端的归属地信息。
     *
     * @return 归属地信息
     */
    public static Optional<String> getCityInfoOptional() {
        return getCityInfoOptional(getClientIpAddress());
    }

    /**
     * 查询指定 IP 的归属地信息。
     *
     * @param ip IP 地址
     * @return 归属地信息
     */
    public static Optional<String> getCityInfoOptional(String ip) {
        return parseIpAddress(ip)
                .map(IPUtils::searchCityInfo);
    }

    /**
     * 获取当前请求客户端 IP。
     *
     * @return 客户端 IP，获取不到时返回 {@code null}
     */
    public static String getClientIpAddress() {
        return getClientIpAddress(ServletUtils.getRequest());
    }

    /**
     * 获取指定请求的客户端 IP。
     *
     * @param request 请求对象
     * @return 客户端 IP，获取不到时返回 {@code null}
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        return getClientIpAddressOptional(request).orElse(null);
    }

    /**
     * 获取当前请求客户端 IP。
     *
     * @return 客户端 IP
     */
    public static Optional<String> getClientIpAddressOptional() {
        return getClientIpAddressOptional(ServletUtils.getRequest());
    }

    /**
     * 获取指定请求的客户端 IP。
     *
     * @param request 请求对象
     * @return 客户端 IP
     */
    public static Optional<String> getClientIpAddressOptional(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        return resolveClientIp(request)
                .map(IPUtils::normalizeLoopbackAddress);
    }

    /**
     * 根据 IP 地址执行离线归属地查询。
     *
     * @param address 已解析的 IP 地址
     * @return 归属地信息
     */
    private static String searchCityInfo(InetAddress address) {
        InetAddress lookupAddress = address.isLoopbackAddress()
                ? parseIpAddress(LOCALHOST_IPV4).orElse(address)
                : address;
        String ip = lookupAddress.getHostAddress();
        try {
            String region = selectSearcher(lookupAddress).search(ip);
            return normalizeRegion(region).orElse(null);
        } catch (Exception exception) {
            log.error("IP 离线归属地查询失败，ip={}", ip, exception);
            throw new ServiceException("IP地址离线归属地查询失败: " + ip, exception);
        }
    }

    /**
     * 解析请求中的真实客户端 IP。
     *
     * @param request 请求对象
     * @return 真实客户端 IP
     */
    private static Optional<String> resolveClientIp(HttpServletRequest request) {
        Optional<String> forwardedIp = extractForwardedHeaderIp(request.getHeader(FORWARDED_HEADER));
        if (forwardedIp.isPresent()) {
            return forwardedIp;
        }
        for (String header : CLIENT_IP_HEADERS) {
            Optional<String> headerIp = extractCandidateIp(request.getHeader(header));
            if (headerIp.isPresent()) {
                return headerIp;
            }
        }
        return extractCandidateIp(request.getRemoteAddr());
    }

    /**
     * 从 Forwarded 请求头中提取客户端 IP。
     *
     * @param forwardedHeader Forwarded 请求头
     * @return 客户端 IP
     */
    private static Optional<String> extractForwardedHeaderIp(String forwardedHeader) {
        if (isBlank(forwardedHeader)) {
            return Optional.empty();
        }
        for (String segment : forwardedHeader.split(",")) {
            for (String pair : segment.split(";")) {
                String candidate = pair.trim();
                if (!candidate.regionMatches(true, 0, "for=", 0, 4)) {
                    continue;
                }
                Optional<String> resolved = extractCandidateIp(candidate.substring(4));
                if (resolved.isPresent()) {
                    return resolved;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 从候选字符串中提取合法 IP。
     *
     * @param rawCandidate 候选字符串
     * @return 合法 IP
     */
    private static Optional<String> extractCandidateIp(String rawCandidate) {
        if (isBlank(rawCandidate)) {
            return Optional.empty();
        }
        return Arrays.stream(rawCandidate.split(","))
                .map(IPUtils::normalizeCandidate)
                .filter(candidate -> !candidate.isEmpty())
                .map(IPUtils::parseIpAddress)
                .flatMap(Optional::stream)
                .map(InetAddress::getHostAddress)
                .map(IPUtils::normalizeLoopbackAddress)
                .findFirst();
    }

    /**
     * 规范化原始 IP 候选值。
     *
     * @param rawCandidate 原始候选值
     * @return 规范化后的候选值
     */
    private static String normalizeCandidate(String rawCandidate) {
        if (rawCandidate == null) {
            return "";
        }
        String candidate = rawCandidate.trim();
        if (candidate.isEmpty()) {
            return "";
        }
        if (candidate.startsWith("\"") && candidate.endsWith("\"") && candidate.length() > 1) {
            candidate = candidate.substring(1, candidate.length() - 1).trim();
        }
        if (UNKNOWN.equalsIgnoreCase(candidate)) {
            return "";
        }
        return stripPort(candidate);
    }

    /**
     * 去除 IP 字符串中的端口信息。
     *
     * @param candidate 候选 IP
     * @return 去端口后的 IP
     */
    private static String stripPort(String candidate) {
        if (candidate.startsWith("[") && candidate.contains("]")) {
            return candidate.substring(1, candidate.indexOf(']')).trim();
        }
        if (candidate.indexOf(':') == candidate.lastIndexOf(':')) {
            int separatorIndex = candidate.indexOf(':');
            if (separatorIndex > 0 && isIpv4Candidate(candidate.substring(0, separatorIndex))) {
                return candidate.substring(0, separatorIndex).trim();
            }
        }
        return candidate;
    }

    /**
     * 解析 IP 文本。
     *
     * @param ip IP 文本
     * @return 解析后的 IP 地址
     */
    private static Optional<InetAddress> parseIpAddress(String ip) {
        if (isBlank(ip)) {
            return Optional.empty();
        }
        String candidate = normalizeCandidate(ip);
        if (!isIpv4Candidate(candidate) && !isIpv6Candidate(candidate)) {
            return Optional.empty();
        }
        try {
            InetAddress address = InetAddress.getByName(candidate);
            if (address instanceof Inet4Address && isIpv4Candidate(candidate)) {
                return Optional.of(address);
            }
            if (address instanceof Inet6Address && isIpv6Candidate(candidate)) {
                return Optional.of(address);
            }
            return Optional.empty();
        } catch (UnknownHostException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 判断字符串是否可能是 IPv4。
     *
     * @param candidate IP 候选值
     * @return 是否为 IPv4 候选值
     */
    private static boolean isIpv4Candidate(String candidate) {
        if (isBlank(candidate)) {
            return false;
        }
        String[] parts = candidate.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }
            for (int index = 0; index < part.length(); index++) {
                if (!Character.isDigit(part.charAt(index))) {
                    return false;
                }
            }
            int value = Integer.parseInt(part);
            if (value < 0 || value > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否可能是 IPv6。
     *
     * @param candidate IP 候选值
     * @return 是否为 IPv6 候选值
     */
    private static boolean isIpv6Candidate(String candidate) {
        if (isBlank(candidate) || !candidate.contains(":")) {
            return false;
        }
        for (int index = 0; index < candidate.length(); index++) {
            char current = candidate.charAt(index);
            if (Character.digit(current, 16) != -1 || current == ':' || current == '.') {
                continue;
            }
            if (current == '%' && index > 0 && index < candidate.length() - 1) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * 规范化本地回环地址。
     *
     * @param ip 原始 IP
     * @return 规范化后的 IP
     */
    private static String normalizeLoopbackAddress(String ip) {
        return parseIpAddress(ip)
                .filter(InetAddress::isLoopbackAddress)
                .map(address -> LOCALHOST_IPV4)
                .orElse(ip);
    }

    /**
     * 清洗 ip2region 原始返回结果。
     *
     * @param region 原始归属地信息
     * @return 清洗后的归属地信息
     */
    private static Optional<String> normalizeRegion(String region) {
        if (isBlank(region)) {
            return Optional.empty();
        }
        String normalized = Arrays.stream(region.split("\\|"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .filter(segment -> !"0".equals(segment))
                .collect(Collectors.joining("|"));
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    /**
     * 选择匹配当前 IP 版本的 searcher。
     *
     * @param address 已解析的 IP 地址
     * @return 对应版本的 searcher
     */
    private static Searcher selectSearcher(InetAddress address) {
        if (address instanceof Inet6Address) {
            return Ipv6SearcherHolder.SEARCHER;
        }
        return Ipv4SearcherHolder.SEARCHER;
    }

    /**
     * 创建指定数据库文件对应的 searcher。
     *
     * @param dataFile 数据库文件名
     * @return searcher 实例
     */
    private static Searcher createSearcher(String dataFile, Version version) {
        ClassPathResource resource = new ClassPathResource(dataFile);
        try (InputStream inputStream = resource.getInputStream()) {
            return Searcher.newWithBuffer(version, Searcher.loadContentFromInputStream(inputStream));
        } catch (IOException exception) {
            log.error("加载 ip2region 数据库失败，file={}", dataFile, exception);
            throw new ServiceException("加载 ip2region 数据库失败: " + dataFile, exception);
        } catch (Exception exception) {
            log.error("初始化 ip2region 检索器失败，file={}", dataFile, exception);
            throw new ServiceException("初始化 ip2region 检索器失败: " + dataFile, exception);
        }
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 待判断值
     * @return 是否为空白
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 延迟加载 IPv4 searcher。
     */
    private static final class Ipv4SearcherHolder {

        private static final Searcher SEARCHER = createSearcher(IPV4_DATA_FILE, Version.IPv4);

        /**
         * 创建内部类实例。
         */
        private Ipv4SearcherHolder() {
        }
    }

    /**
     * 延迟加载 IPv6 searcher。
     */
    private static final class Ipv6SearcherHolder {

        private static final Searcher SEARCHER = createSearcher(IPV6_DATA_FILE, Version.IPv6);

        /**
         * 创建内部类实例。
         */
        private Ipv6SearcherHolder() {
        }
    }
}
