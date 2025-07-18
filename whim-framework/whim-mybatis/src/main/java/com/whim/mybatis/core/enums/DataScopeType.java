package com.whim.mybatis.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jince
 * @date 2025/7/4 12:38
 * @description 数据权限类型枚举
 */
@Getter
@AllArgsConstructor
public enum DataScopeType {
    /**
     * 全部数据权限
     */
    ALL("1", ""),
    /**
     * 自定义数据权限
     */
    CUSTOM("2", "#{#deptName} in (#{@systemDataScope.getRoleCustomDataPermission(#roleId)})"),
    /**
     * 本部门数据权限
     */
    DEPT("3", "#{#deptName}=#{#userInfo.deptId}"),
    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD("4", "#{#deptName} in (#{@systemDataScope.getDeptAndChildDept(#userInfo.deptId)})"),
    /**
     * 只本人数据权限
     */
    SELF("5", "#{#userName}=#{#userInfo.userId}"),
    /**
     * 本部门及以下数据权限或本人数据权限
     */
    DEPT_AND_CHILD_OR_SELF("6", "#{#deptName} in (#{@systemDataScope.getDeptAndChildDept(#userInfo.deptId)}) or #{#userName}=#{#user.userId}");

    private final String code;
    private final String sqlTemplate;
    private static final Map<String, DataScopeType> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(DataScopeType::getCode, Function.identity()));

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return DataScopeType
     */
    public static DataScopeType findByCode(String code) {
        if (code == null) return null;
        return CODE_MAP.get(code);
    }
}
