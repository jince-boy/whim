package utils;

import java.util.UUID;

/**
 * @author Jince
 * date: 2024/10/5 00:16
 * description: id生成工具类
 */
public class IDGeneratorUtils {
    /**
     * 生成UUID
     *
     * @return UUID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成UUID，去掉“-”
     *
     * @return UUID
     */
    public static String generateIdCompact() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
