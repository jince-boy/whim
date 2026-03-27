package com.whim.json.spi;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 默认脱敏访问评估器，在未接入具体安全框架时始终不放行原始值。
 */
public final class DefaultDesensitizationAccessEvaluator implements DesensitizationAccessEvaluator {

    /**
     * 判断当前上下文是否允许查看原始值。
     *
     * @param roles 允许查看原始值的角色
     * @param authorities 允许查看原始值的权限
     * @param requireAll 是否要求角色与权限全部命中
     * @return 始终返回 false
     */
    @Override
    public boolean canViewPlainValue(String[] roles, String[] authorities, boolean requireAll) {
        return false;
    }
}
