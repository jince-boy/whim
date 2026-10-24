package com.whim.excel.core;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 动态下拉选项扩展接口，实现类由 Spring 管理。
 */
@FunctionalInterface
public interface ExcelOptionsProvider {
    /** 返回按显示顺序排列的选项，查询应遵守业务数据权限。 */
    List<String> getOptions();
}
