package com.whim.core.utils;

import com.whim.core.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;

/**
 * @author Jince
 * @date 2024/10/5 00:19
 * @description IP工具类
 */
@Slf4j
public class IPUtils {

    public final static String IP_DATA = "ip2region.xdb";
    private static final Searcher SEARCHER;

    static {
        try {
            ClassPathResource resource = new ClassPathResource(IP_DATA);
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] dbBytes = StreamUtils.copyToByteArray(inputStream);
                SEARCHER = Searcher.newWithBuffer(dbBytes);
            }
        } catch (Exception e) {
            throw new ServiceException("IPUtils初始化失败，原因：" + e.getMessage());
        }
    }

    /**
     * 获取当前请求客户端的IP地址城市信息
     *
     * @return 城市信息
     */
    public static String getCityInfo() {
        return getCityInfo(getClientIpAddress());
    }

    /**
     * 获取指定IP地址的城市信息
     *
     * @param ip IP地址
     * @return 城市信息
     */
    public static String getCityInfo(String ip) {
        try {
            String search = SEARCHER.search(ip.trim());
            return search.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", ip);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public static String getClientIpAddress() {
        return getClientIpAddress(null);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            request = ServletUtils.getRequest();
        }

        String ip = getHeaderIp(request,
                "x-forwarded-for",
                "Proxy-Client-IP",
                "X-Forwarded-For",
                "WL-Proxy-Client-IP",
                "X-Real-IP");

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 从请求头中获取第一个有效的IP地址
     *
     * @param request HttpServletRequest
     * @param headers 要检查的请求头
     * @return 第一个有效的IP地址
     */
    private static String getHeaderIp(HttpServletRequest request, String... headers) {
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (isUnknown(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    return subIp.trim(); // 去除可能的空格
                }
            }
        }
        return ip;
    }

    /**
     * 判断是否为unknown
     *
     * @param checkString 要检查的字符串
     * @return true表示为unknown，false表示不为unknown
     */
    private static boolean isUnknown(String checkString) {
        return checkString != null && !checkString.trim().isEmpty() && !"unknown".equalsIgnoreCase(checkString);
    }
}
