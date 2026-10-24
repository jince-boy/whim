package com.whim.excel.convert;

import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.apache.fesod.sheet.util.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Long、BigInteger 和 BigDecimal 精度保护，超出十五位有效数字时按文本写出。
 */
@RequiredArgsConstructor
public final class ExcelNumberConverter implements Converter<Number> {

    private final Class<?> javaType;
    private final CellDataTypeEnum excelType;

    /** 返回注册的数字类型。 */
    @Override
    public Class<?> supportJavaTypeKey() {
        return javaType;
    }

    /** 返回读取单元格类型；导出注册为 null，匹配未指定目标类型的普通字段。 */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return excelType;
    }

    /** 精确读取数字或文本，拒绝整数小数截断和溢出。 */
    @Override
    public Number convertToJavaData(
        ReadCellData<?> cell,
        ExcelContentProperty property,
        GlobalConfiguration configuration
    ) throws ParseException {
        if (
            cell.getType() != CellDataTypeEnum.NUMBER &&
            (cell.getStringValue() == null || cell.getStringValue().isBlank())
        ) {
            return null;
        }
        BigDecimal value =
            cell.getType() == CellDataTypeEnum.NUMBER
                ? cell.getNumberValue()
                : NumberUtils.parseBigDecimal(cell.getStringValue().strip(), property);
        if (javaType == Long.class || javaType == long.class) {
            return value.longValueExact();
        }
        if (javaType == BigInteger.class) {
            return value.toBigIntegerExact();
        }
        return value;
    }

    /** 保留高精度数字的完整十进制内容，其余数字保持数值单元格。 */
    @Override
    public WriteCellData<?> convertToExcelData(
        Number value,
        ExcelContentProperty property,
        GlobalConfiguration configuration
    ) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        BigDecimal decimal = value instanceof BigDecimal amount ? amount : new BigDecimal(value.toString());
        if (Math.abs((long) decimal.scale()) > 300) {
            return new WriteCellData<>(decimal.toString());
        }
        if (decimal.precision() > 15) {
            return new WriteCellData<>(decimal.toPlainString());
        }
        return NumberUtils.formatToCellData(decimal, property);
    }
}
