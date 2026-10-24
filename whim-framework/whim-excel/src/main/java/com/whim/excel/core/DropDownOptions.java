package com.whim.excel.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 按字段名定位的普通或两级联动下拉，自动适配导出列排序。
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropDownOptions {

    private final String fieldName;
    private final List<String> options;
    private final String nextFieldName;
    private final Map<String, List<String>> nextOptions;

    /** 创建普通下拉选项，允许中文、空格、标点和数字开头的显示值。 */
    public static DropDownOptions of(String fieldName, List<String> options) {
        Assert.hasText(fieldName, "下拉字段名不能为空");
        Assert.notNull(options, "下拉选项不能为空");
        return new DropDownOptions(fieldName, List.copyOf(options), null, Map.of());
    }

    /** 创建两级联动选项，Map 的键为父级显示值，值为对应子级列表。 */
    public static DropDownOptions linked(String fieldName, String nextFieldName, Map<String, List<String>> options) {
        Assert.hasText(fieldName, "一级下拉字段名不能为空");
        Assert.hasText(nextFieldName, "二级下拉字段名不能为空");
        Assert.isTrue(!fieldName.equals(nextFieldName), "两级下拉不能使用同一字段");
        Assert.notEmpty(options, "级联下拉选项不能为空");
        Map<String, List<String>> copy = new LinkedHashMap<>();
        options.forEach((parent, children) -> {
            Assert.hasText(parent, "一级下拉选项不能为空");
            Assert.notNull(children, "二级下拉选项列表不能为空");
            copy.put(parent, List.copyOf(children));
        });
        return new DropDownOptions(
            fieldName,
            List.copyOf(copy.keySet()),
            nextFieldName,
            Collections.unmodifiableMap(copy)
        );
    }
}
