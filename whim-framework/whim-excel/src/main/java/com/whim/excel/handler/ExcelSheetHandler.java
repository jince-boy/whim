package com.whim.excel.handler;

import com.whim.core.utils.IdUtils;
import com.whim.excel.annotation.ExcelDictFormat;
import com.whim.excel.annotation.ExcelDynamicOptions;
import com.whim.excel.annotation.ExcelEnumFormat;
import com.whim.excel.annotation.ExcelNotation;
import com.whim.excel.annotation.ExcelRequired;
import com.whim.excel.core.DropDownOptions;
import com.whim.excel.core.ExcelOperationContext;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.write.handler.CellWriteHandler;
import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.handler.context.CellWriteHandlerContext;
import org.apache.fesod.sheet.write.handler.context.SheetWriteHandlerContext;
import org.apache.fesod.sheet.write.metadata.style.WriteCellStyle;
import org.apache.fesod.sheet.write.metadata.style.WriteFont;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 根据最终表头生成样式、批注与下拉，级联下拉每列只生成一条区域规则。
 */
@RequiredArgsConstructor
public final class ExcelSheetHandler implements SheetWriteHandler, CellWriteHandler {

    private final List<DropDownOptions> configuredOptions;
    private final boolean dropdown;
    private final int dropdownRows;
    private final boolean freezeHead;

    /** 根据实际列位置和表头高度创建下拉，适配列筛选、重排和多级标题。 */
    @Override
    public void afterSheetCreate(SheetWriteHandlerContext context) {
        var holder = context.getWriteSheetHolder();
        int headRows = holder.needHead() ? holder.getExcelWriteHeadProperty().getHeadRowNumber() : 0;
        int firstRow = holder.relativeHeadRowIndex() + headRows;
        Sheet sheet = holder.getSheet();
        if (freezeHead && firstRow > 0) {
            sheet.createFreezePane(0, firstRow);
        }
        if (!dropdown) {
            return;
        }
        Assert.isTrue(
            (long) firstRow + dropdownRows <= SpreadsheetVersion.EXCEL2007.getMaxRows(),
            "下拉校验区域超过 Excel 最大行数"
        );
        Map<String, Integer> columns = new HashMap<>();
        Map<String, DropDownOptions> options = new LinkedHashMap<>();
        for (Head head : holder.getExcelWriteHeadProperty().getHeadMap().values()) {
            Field field = head.getField();
            if (field == null) {
                continue;
            }
            columns.put(field.getName(), head.getColumnIndex());
            ExcelDynamicOptions dynamic = field.getAnnotation(ExcelDynamicOptions.class);
            ExcelDictFormat dictionary = field.getAnnotation(ExcelDictFormat.class);
            ExcelEnumFormat enumeration = field.getAnnotation(ExcelEnumFormat.class);
            List<String> values = List.of();
            if (dynamic != null) {
                values = ExcelOperationContext.current().getOptions(dynamic.providerClass());
            } else if (
                (dictionary != null && dictionary.dropdown()) || (enumeration != null && enumeration.dropdown())
            ) {
                values = new ArrayList<>(ExcelOperationContext.current().getMapping(field).values());
            }
            if (!values.isEmpty()) {
                options.put(field.getName(), DropDownOptions.of(field.getName(), values));
            }
        }
        // 显式选项覆盖注解，级联子列不再额外生成一条普通下拉规则。
        for (DropDownOptions option : configuredOptions) {
            options.put(option.getFieldName(), option);
        }
        Set<String> children = new HashSet<>();
        Set<String> parents = new HashSet<>();
        configuredOptions
            .stream()
            .filter(option -> option.getNextFieldName() != null)
            .forEach(option -> parents.add(option.getFieldName()));
        configuredOptions
            .stream()
            .filter(option -> option.getNextFieldName() != null)
            .forEach(option -> {
                Assert.isTrue(children.add(option.getNextFieldName()), "同一列不能重复配置级联子选项");
                Assert.isTrue(!parents.contains(option.getNextFieldName()), "级联下拉仅支持两级，不能形成循环或多级链");
                options.remove(option.getNextFieldName());
            });
        for (DropDownOptions option : options.values()) {
            Integer column = columns.get(option.getFieldName());
            if (column == null) {
                continue;
            }
            if (option.getNextFieldName() == null) {
                addSimpleOptions(sheet, column, firstRow, option.getOptions());
            } else {
                Integer nextColumn = columns.get(option.getNextFieldName());
                Assert.notNull(nextColumn, "级联下拉的子字段必须包含在导出列中：" + option.getNextFieldName());
                addLinkedOptions(sheet, column, nextColumn, firstRow, option);
            }
        }
    }

    /** 仅修改末级表头，按字段关联样式，避免相同标题误匹配。 */
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        if (
            !Boolean.TRUE.equals(context.getHead()) ||
            context.getHeadData() == null ||
            context.getHeadData().getField() == null ||
            context.getFirstCellData() == null
        ) {
            return;
        }
        List<String> titles = context.getHeadData().getHeadNameList();
        int leafRow = titles.size() - 1;
        while (leafRow > 0 && titles.get(leafRow - 1).equals(titles.getLast())) {
            leafRow--;
        }
        if (context.getRelativeRowIndex() != leafRow) {
            return;
        }
        Field field = context.getHeadData().getField();
        if (field.isAnnotationPresent(ExcelRequired.class)) {
            WriteCellStyle style = context.getFirstCellData().getOrCreateStyle();
            WriteFont font = style.getWriteFont();
            if (font == null) {
                font = new WriteFont();
            }
            font.setColor(IndexedColors.RED.getIndex());
            style.setWriteFont(font);
        }
        ExcelNotation notation = field.getAnnotation(ExcelNotation.class);
        if (notation == null) {
            return;
        }
        Sheet sheet = context.getCell().getSheet();
        var helper = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(context.getColumnIndex());
        anchor.setCol2(Math.min(context.getColumnIndex() + 3, 16_383));
        anchor.setRow1(context.getRowIndex());
        anchor.setRow2(context.getRowIndex() + 4);
        Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        comment.setString(helper.createRichTextString(notation.value()));
        comment.setAuthor("Whim");
        context.getCell().setCellComment(comment);
    }

    /** 使用命名区域承载选项，避免内联列表的长度、逗号和转义限制。 */
    private void addSimpleOptions(Sheet sheet, int column, int firstRow, List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        validateOptions(values);
        Assert.isTrue(values.size() <= SpreadsheetVersion.EXCEL2007.getMaxRows(), "下拉选项超过 Excel 最大行数");
        Workbook workbook = sheet.getWorkbook();
        String identifier = IdUtils.uuid32();
        Sheet hidden = workbook.createSheet("_whim_" + identifier.substring(0, 24));
        for (int index = 0; index < values.size(); index++) {
            hidden.createRow(index).createCell(0).setCellValue(values.get(index));
        }
        workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);
        String rangeName = "whim_" + identifier;
        addName(workbook, rangeName, "'" + hidden.getSheetName() + "'!$A$1:$A$" + values.size());
        addValidation(sheet, column, firstRow, rangeName);
    }

    /** 用独立名称映射显示标签，父项无需满足 Excel 名称语法。 */
    private void addLinkedOptions(
        Sheet sheet,
        int parentColumn,
        int childColumn,
        int firstRow,
        DropDownOptions option
    ) {
        validateOptions(option.getOptions());
        Workbook workbook = sheet.getWorkbook();
        String identifier = IdUtils.uuid32();
        String prefix = "whim_" + identifier;
        Sheet hidden = workbook.createSheet("_whim_" + identifier.substring(0, 24));
        String reference = "'" + hidden.getSheetName() + "'!";
        List<String> values = new ArrayList<>();
        List<String> childRanges = new ArrayList<>();
        for (int index = 0; index < option.getOptions().size(); index++) {
            String parent = option.getOptions().get(index);
            List<String> children = option.getNextOptions().get(parent);
            validateOptions(children);
            String name = prefix + "_child_" + index;
            childRanges.add(name);
            int start = values.size() + 1;
            if (children.isEmpty()) {
                values.add("");
            } else {
                values.addAll(children);
            }
            Assert.isTrue(values.size() < SpreadsheetVersion.EXCEL2007.getMaxRows(), "级联下拉选项超过 Excel 最大行数");
            addName(workbook, name, reference + "$C$" + start + ":$C$" + values.size());
        }
        int count = Math.max(values.size(), option.getOptions().size());
        Assert.isTrue(count < SpreadsheetVersion.EXCEL2007.getMaxRows(), "级联下拉选项超过 Excel 最大行数");
        for (int index = 0; index < count; index++) {
            Row row = hidden.createRow(index);
            if (index < option.getOptions().size()) {
                row.createCell(0).setCellValue(option.getOptions().get(index));
                row.createCell(1).setCellValue(childRanges.get(index));
            }
            if (index < values.size()) {
                row.createCell(2).setCellValue(values.get(index));
            }
        }
        hidden.createRow(count).createCell(2).setBlank();
        workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);
        addName(workbook, prefix + "_parents", reference + "$A$1:$A$" + option.getOptions().size());
        addName(workbook, prefix + "_lookup", reference + "$A$1:$B$" + option.getOptions().size());
        addName(workbook, prefix + "_empty", reference + "$C$" + (count + 1));
        addValidation(sheet, parentColumn, firstRow, prefix + "_parents");
        String parentCell = "$" + CellReference.convertNumToColString(parentColumn) + (firstRow + 1);
        String formula =
            "INDIRECT(IFERROR(VLOOKUP(" + parentCell + "," + prefix + "_lookup,2,FALSE),\"" + prefix + "_empty\"))";
        addValidation(sheet, childColumn, firstRow, formula);
    }

    /** 拒绝空白、重复或超长选项，避免生成有歧义的下拉。 */
    private void validateOptions(List<String> values) {
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            Assert.hasText(value, "Excel 下拉选项不能为空");
            Assert.isTrue(value.length() <= 32_767, "Excel 下拉选项长度不能超过 32767 个字符");
            Assert.isTrue(seen.add(value.toLowerCase(java.util.Locale.ROOT)), "Excel 下拉选项不能重复或仅大小写不同");
        }
    }

    /** 注册只含内部生成标识的命名区域，不把业务标签作为公式名称。 */
    private void addName(Workbook workbook, String name, String formula) {
        Name range = workbook.createName();
        range.setNameName(name);
        range.setRefersToFormula(formula);
    }

    /** 为整段数据区域添加单条校验，公式中的行号按目标单元格相对移动。 */
    private void addValidation(Sheet sheet, int column, int firstRow, String formula) {
        var helper = sheet.getDataValidationHelper();
        var range = new CellRangeAddressList(firstRow, firstRow + dropdownRows - 1, column, column);
        DataValidation validation = helper.createValidation(helper.createFormulaListConstraint(formula), range);
        validation.setEmptyCellAllowed(true);
        validation.setSuppressDropDownArrow(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("填写内容无效", "请选择下拉列表中的选项");
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
}
