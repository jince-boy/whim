package com.whim.json.spi;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 脱敏访问评估器，用于判断当前调用方是否有权查看字段原始值。
 */
public interface DesensitizationAccessEvaluator {

    /**
     * 判断当前上下文是否允许查看原始值。
     *
     * @param roles 允许查看原始值的角色
     * @param authorities 允许查看原始值的权限
     * @param requireAll 是否要求角色与权限全部命中
     * @return 为 true 表示允许查看原始值
     */
    boolean canViewPlainValue(String[] roles, String[] authorities, boolean requireAll);
}
