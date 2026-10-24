package com.whim.excel.convert;

import com.whim.core.utils.ValueParserUtils;
import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.core.ExcelOperationContext;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典和枚举双向转换，未知值立即报错，避免静默丢失业务数据。
 */
public final class ExcelMappingConverter implements Converter<Object> {

    /** 将显示文本转换为字段声明的类型，空白值交由字段校验处理。 */
    @Override
    public Object convertToJavaData(
        ReadCellData<?> cell,
        ExcelContentProperty property,
        GlobalConfiguration configuration
    ) {
        String text = switch (cell.getType()) {
            case EMPTY -> null;
            case STRING, DIRECT_STRING, RICH_TEXT_STRING -> cell.getStringValue();
            case NUMBER -> cell.getNumberValue().stripTrailingZeros().toPlainString();
            case BOOLEAN -> cell.getBooleanValue().toString();
            default -> throw new IllegalArgumentException("字典或枚举单元格格式不正确");
        };
        if (text == null || text.isBlank()) {
            return null;
        }
        Field field = property.getField();
        String value = convert(text.strip(), field, ExcelOperationContext.current().getReverseMapping(field));
        return DefaultConversionService.getSharedInstance().convert(value, field.getType());
    }

    /** 将字段值转换为显示文本；枚举对象按常量名称映射。 */
    @Override
    public WriteCellData<String> convertToExcelData(
        Object value,
        ExcelContentProperty property,
        GlobalConfiguration configuration
    ) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        String text = value instanceof Enum<?> constant ? constant.name() : ValueParserUtils.toString(value);
        if (text.isBlank()) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(
            convert(text, property.getField(), ExcelOperationContext.current().getMapping(property.getField()))
        );
    }

    /** 保持输入顺序转换单值或分隔符连接的多值，任一未知项使整格失败。 */
    private String convert(String text, Field field, Map<String, String> mapping) {
        if (mapping.containsKey(text)) {
            return mapping.get(text);
        }
        ExcelDictFormat dictionary = field.getAnnotation(ExcelDictFormat.class);
        if (dictionary == null || !text.contains(dictionary.separator())) {
            throw new IllegalArgumentException("字段「" + field.getName() + "」包含未配置的字典或枚举值");
        }
        List<String> converted = new ArrayList<>();
        for (String item : text.split(Pattern.quote(dictionary.separator()), -1)) {
            String value = mapping.get(item.strip());
            if (value == null) {
                throw new IllegalArgumentException("字段「" + field.getName() + "」包含无效的多选值");
            }
            converted.add(value);
        }
        return String.join(dictionary.separator(), converted);
    }
}
