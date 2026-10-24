package com.whim.excel.core;

import com.whim.core.utils.ValueParserUtils;
import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.annotation.ExcelEnumFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 单次操作的字典和选项快照，ScopedValue 在操作结束后自动释放。
 */
@RequiredArgsConstructor
public final class ExcelOperationContext {

    private static final ScopedValue<ExcelOperationContext> CURRENT = ScopedValue.newInstance();
    private final ExcelDictionaryProvider dictionaryProvider;
    private final ListableBeanFactory beanFactory;
    private final Map<String, Map<String, String>> dictionaries = new HashMap<>();
    private final Map<Field, Map<String, String>> mappings = new HashMap<>();
    private final Map<Field, Map<String, String>> reverseMappings = new HashMap<>();
    private final Map<Class<? extends ExcelOptionsProvider>, List<String>> options = new HashMap<>();

    /** 在当前线程绑定独立操作上下文，支持嵌套调用。 */
    public <T> T execute(Supplier<T> action) {
        return ScopedValue.where(CURRENT, this).call(action::get);
    }

    /** 获取当前操作，转换器应通过 ExcelBuilder 或 ExcelService 调用。 */
    public static ExcelOperationContext current() {
        if (!CURRENT.isBound()) {
            throw new IllegalStateException("Excel 业务转换器必须通过 ExcelBuilder 或 ExcelService 使用");
        }
        return CURRENT.get();
    }

    /** 获取本次操作内一致的动态选项。 */
    public List<String> getOptions(Class<? extends ExcelOptionsProvider> providerClass) {
        return options.computeIfAbsent(providerClass, type -> List.copyOf(beanFactory.getBean(type).getOptions()));
    }

    /** 获取编码到显示文本的映射，同时检查双向转换是否存在歧义。 */
    public Map<String, String> getMapping(Field field) {
        return mappings.computeIfAbsent(field, key -> {
            ExcelDictFormat dictionary = key.getAnnotation(ExcelDictFormat.class);
            ExcelEnumFormat enumeration = key.getAnnotation(ExcelEnumFormat.class);
            Assert.isTrue(
                (dictionary == null) != (enumeration == null),
                "Excel 转换字段必须且只能声明一个字典或枚举注解：" + key.getName()
            );
            Map<String, String> result = new LinkedHashMap<>();
            if (dictionary != null) {
                Assert.hasText(dictionary.separator(), "字典多值分隔符不能为空");
                boolean systemDictionary = StringUtils.hasText(dictionary.dictType());
                Assert.isTrue(
                    systemDictionary != StringUtils.hasText(dictionary.readConverterExp()),
                    "字典类型与固定表达式必须且只能配置一个：" + key.getName()
                );
                if (systemDictionary) {
                    result.putAll(
                        dictionaries.computeIfAbsent(dictionary.dictType(), type -> {
                            Assert.state(dictionaryProvider != null, "未配置 ExcelDictionaryProvider 字典提供者");
                            Map<String, String> values = dictionaryProvider.getDictionary(type);
                            Assert.notEmpty(values, "Excel 字典不存在或没有可用选项：" + type);
                            return Collections.unmodifiableMap(new LinkedHashMap<>(values));
                        })
                    );
                } else {
                    for (String expression : dictionary.readConverterExp().split(",", -1)) {
                        String[] entry = expression.split("=", 2);
                        Assert.isTrue(entry.length == 2, "Excel 字典表达式格式错误：" + key.getName());
                        Assert.isTrue(
                            result.putIfAbsent(entry[0].strip(), entry[1].strip()) == null,
                            "Excel 字典编码重复：" + key.getName()
                        );
                    }
                }
            } else {
                Assert.isTrue(
                    !key.getType().isEnum() || key.getType().equals(enumeration.enumClass()),
                    "Excel 枚举字段类型与注解配置不一致：" + key.getName()
                );
                for (Enum<?> constant : enumeration.enumClass().getEnumConstants()) {
                    var accessor = PropertyAccessorFactory.forBeanPropertyAccess(constant);
                    String code = key.getType().isEnum()
                        ? constant.name()
                        : ValueParserUtils.toString(accessor.getPropertyValue(enumeration.codeField()));
                    String label = ValueParserUtils.toString(accessor.getPropertyValue(enumeration.textField()));
                    Assert.hasText(code, "Excel 枚举编码不能为空：" + key.getName());
                    Assert.hasText(label, "Excel 枚举文本不能为空：" + key.getName());
                    Assert.isTrue(result.putIfAbsent(code, label) == null, "Excel 枚举编码重复：" + key.getName());
                }
            }
            Map<String, String> reverse = new LinkedHashMap<>();
            result.forEach((code, label) -> {
                Assert.hasText(code, "Excel 字典编码不能为空：" + key.getName());
                Assert.hasText(label, "Excel 字典文本不能为空：" + key.getName());
                if (dictionary != null && !dictionary.dropdown()) {
                    Assert.isTrue(
                        !code.contains(dictionary.separator()) && !label.contains(dictionary.separator()),
                        "多值字典的编码和文本不能包含多值分隔符：" + key.getName()
                    );
                }
                Assert.isTrue(reverse.putIfAbsent(label, code) == null, "Excel 显示文本重复：" + key.getName());
            });
            reverseMappings.put(key, Collections.unmodifiableMap(reverse));
            return Collections.unmodifiableMap(result);
        });
    }

    /** 获取经过唯一性校验的显示文本到编码映射。 */
    public Map<String, String> getReverseMapping(Field field) {
        getMapping(field);
        return reverseMappings.get(field);
    }
}
