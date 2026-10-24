package com.whim.excel.handler;

import com.whim.excel.annotation.CellMerge;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.write.handler.CellWriteHandler;
import org.apache.fesod.sheet.write.handler.RowWriteHandler;
import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.handler.context.CellWriteHandlerContext;
import org.apache.fesod.sheet.write.handler.context.RowWriteHandlerContext;
import org.apache.fesod.sheet.write.handler.context.SheetWriteHandlerContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 按相邻原始值流式合并，只保存当前行和各列的当前分组，不回读已刷盘行。
 */
public final class CellMergeHandler implements CellWriteHandler, RowWriteHandler, SheetWriteHandler {

    private final Map<Sheet, List<MergeGroup>> groups = new IdentityHashMap<>();
    private final Map<Sheet, Map<String, Object>> rowValues = new IdentityHashMap<>();

    /** 依据最终列映射初始化需要合并的列，校验分组依赖字段。 */
    @Override
    public void afterSheetCreate(SheetWriteHandlerContext context) {
        Map<String, Head> heads = new LinkedHashMap<>();
        var headProperty = context.getWriteSheetHolder().getExcelWriteHeadProperty();
        for (Head head : headProperty.getHeadMap().values()) {
            if (head.getField() != null) {
                heads.put(head.getFieldName(), head);
            }
        }
        List<MergeGroup> sheetGroups = new ArrayList<>();
        for (Head head : heads.values()) {
            CellMerge merge = head.getField().getAnnotation(CellMerge.class);
            if (merge == null) {
                continue;
            }
            for (String dependency : merge.mergeBy()) {
                Assert.isTrue(heads.containsKey(dependency), "合并依赖字段必须包含在导出列中：" + dependency);
            }
            sheetGroups.add(new MergeGroup(head.getFieldName(), head.getColumnIndex(), merge.mergeBy()));
        }
        groups.put(context.getWriteSheetHolder().getSheet(), sheetGroups);
        rowValues.put(context.getWriteSheetHolder().getSheet(), new HashMap<>());
    }

    /** 收集本行导出字段的原始值，保持编码不同但显示文本相同的数据独立。 */
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        if (
            Boolean.TRUE.equals(context.getHead()) ||
            context.getHeadData() == null ||
            context.getHeadData().getFieldName() == null
        ) {
            return;
        }
        Map<String, Object> values = rowValues.get(context.getCell().getSheet());
        if (values != null) {
            values.put(context.getHeadData().getFieldName(), context.getOriginalValue());
        }
    }

    /** 在当前行仍驻留内存时清空重复内容并推进合并分组。 */
    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        if (Boolean.TRUE.equals(context.getHead())) {
            return;
        }
        Sheet sheet = context.getRow().getSheet();
        Map<String, Object> values = rowValues.get(sheet);
        if (values == null) {
            return;
        }
        int row = context.getRowIndex();
        for (MergeGroup group : groups.get(sheet)) {
            Object value = values.get(group.fieldName);
            boolean blank = value == null || (value instanceof String text && text.isBlank());
            List<Object> key = new ArrayList<>();
            key.add(value);
            for (String dependency : group.dependencies) {
                key.add(values.get(dependency));
            }
            if (!blank && group.firstRow >= 0 && row == group.lastRow + 1 && Objects.equals(group.key, key)) {
                group.lastRow = row;
                Cell cell = context.getRow().getCell(group.column);
                if (cell != null) {
                    cell.setBlank();
                }
            } else {
                finishGroup(sheet, group);
                group.firstRow = blank ? -1 : row;
                group.lastRow = row;
                group.key = key;
            }
        }
        values.clear();
    }

    /** 工作簿结束时完成最后一个分组，支持跨批次但不跨 Sheet 合并。 */
    @Override
    public void afterSheetDispose(SheetWriteHandlerContext context) {
        Sheet sheet = context.getWriteSheetHolder().getSheet();
        List<MergeGroup> pending = groups.remove(sheet);
        if (pending != null) {
            pending.forEach(group -> finishGroup(sheet, group));
        }
        rowValues.remove(sheet);
    }

    /** 仅添加至少两行的合并区间，沿用 POI 的区域冲突检查。 */
    private void finishGroup(Sheet sheet, MergeGroup group) {
        if (group.firstRow >= 0 && group.lastRow > group.firstRow) {
            sheet.addMergedRegion(new CellRangeAddress(group.firstRow, group.lastRow, group.column, group.column));
        }
    }

    /**
     * @author Jince
     * @date 2026/10/25
     * @description 单列当前合并分组状态。
     */
    @RequiredArgsConstructor
    private static final class MergeGroup {

        private final String fieldName;
        private final int column;
        private final String[] dependencies;
        private int firstRow = -1;
        private int lastRow = -1;
        private List<Object> key;
    }
}
