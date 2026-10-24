package com.whim.excel.core;

import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.builder.ExcelWriterSheetBuilder;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 同一工作簿的多表写入会话，允许不同模型并统一管理 writer 生命周期。
 */
public final class ExcelWriterSession<T> {

    private final ExcelWriter writer;
    private final Class<T> defaultType;
    private final String defaultSheet;
    private final Thread ownerThread = Thread.currentThread();
    private final Map<String, WriteSheet> sheets = new LinkedHashMap<>();
    private final Map<String, Class<?>> types = new LinkedHashMap<>();
    private boolean active = true;

    /** 创建受当前导出回调管理的写入会话。 */
    public ExcelWriterSession(ExcelWriter writer, Class<T> defaultType, String defaultSheet) {
        this.writer = writer;
        this.defaultType = defaultType;
        this.defaultSheet = defaultSheet;
    }

    /** 追加数据到默认工作表，重复调用即可分批写入。 */
    public void write(Collection<T> data) {
        write(defaultSheet, defaultType, data);
    }

    /** 按名称和模型写入工作表，同名表必须使用相同模型。 */
    public <R> void write(String sheetName, Class<R> headType, Collection<R> data) {
        checkActive();
        Assert.hasText(sheetName, "工作表名称不能为空");
        Assert.notNull(data, "Excel 写入数据不能为空，请使用空集合表示空表");
        String key = sheetName.toLowerCase(Locale.ROOT);
        if (!sheets.containsKey(key)) {
            configureSheet(sheetName, headType, builder -> {});
        }
        Assert.isTrue(types.get(key).equals(headType), "同一工作表不能使用不同的数据模型");
        writer.write(data, sheets.get(key));
    }

    /** 在首次写入前配置单张表的表头、样式、处理器或列筛选。 */
    public void configureSheet(String sheetName, Class<?> headType, Consumer<ExcelWriterSheetBuilder> configuration) {
        checkActive();
        Assert.hasText(sheetName, "工作表名称不能为空");
        Assert.notNull(headType, "Excel 表头类型不能为空");
        Assert.notNull(configuration, "工作表配置回调不能为空");
        WorkbookUtil.validateSheetName(sheetName);
        String key = sheetName.toLowerCase(Locale.ROOT);
        Assert.isTrue(!sheets.containsKey(key), "已创建的工作表不能重复配置：" + sheetName);
        ExcelWriterSheetBuilder builder = FesodSheet.writerSheet(sheets.size(), sheetName).head(headType);
        configuration.accept(builder);
        // 表名和编号属于会话管理范围，配置回调不能破坏后续写入定位。
        builder.sheetNo(sheets.size()).sheetName(sheetName);
        sheets.put(key, builder.build());
        types.put(key, headType);
    }

    /** 回调结束后使会话失效，防止逃逸到异步线程或被重复使用。 */
    public void finishSession() {
        active = false;
    }

    /** 工作簿只能在创建会话的线程、回调的有效期内写入。 */
    private void checkActive() {
        Assert.state(active, "Excel 写入会话已经结束");
        Assert.state(Thread.currentThread() == ownerThread, "Excel 写入会话不能跨线程使用");
    }
}
