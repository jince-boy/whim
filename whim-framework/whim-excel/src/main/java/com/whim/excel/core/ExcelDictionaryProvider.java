package com.whim.excel.core;

import java.util.Map;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典扩展接口，由应用装配业务实现，Excel 模块不依赖系统业务。
 */
@FunctionalInterface
public interface ExcelDictionaryProvider {
    /** 返回按显示顺序排列的字典编码与标签；同次操作只加载一次同类型字典。 */
    Map<String, String> getDictionary(String dictionaryType);
}
