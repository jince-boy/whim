package com.whim.excel.builder;

import com.whim.core.exception.ServiceException;
import com.whim.excel.convert.ExcelNumberConverter;
import com.whim.excel.core.DropDownOptions;
import com.whim.excel.core.ExcelPageIterator;
import com.whim.excel.core.ExcelWriterSession;
import com.whim.excel.handler.CellMergeHandler;
import com.whim.excel.handler.ExcelSheetHandler;
import com.whim.excel.service.ExcelService;
import com.whim.excel.utils.ExcelResponseUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.write.builder.ExcelWriterBuilder;
import org.apache.fesod.sheet.write.handler.WriteHandler;
import org.apache.fesod.sheet.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.fesod.sheet.write.style.column.SimpleColumnWidthStyleStrategy;
import org.apache.fesod.sheet.write.style.row.SimpleRowHeightStyleStrategy;
import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 普通、分页、多表和 ZIP 流式导出，构造器为一次性对象。
 */
public final class ExcelExportBuilder<T> {

    private final ExcelService service;
    private final Class<T> headType;
    private final List<Converter<?>> converters = new ArrayList<>();
    private final List<Supplier<? extends WriteHandler>> handlers = new ArrayList<>();
    private Supplier<Iterator<T>> rows;
    private String sheetName = "数据";
    private String filename;
    private String password;
    private boolean needHead = true;
    private boolean merge;
    private boolean dropdown = true;
    private boolean freezeHead = true;
    private boolean used;
    private int dropdownRows;
    private int batchSize;
    private int rowsPerSheet = 1_000_000;
    private Integer zipRows;
    private Integer columnWidth;
    private Short headRowHeight;
    private Short contentRowHeight;
    private Collection<String> includeFields;
    private Collection<String> excludeFields;
    private boolean orderByIncludeColumn;
    private List<DropDownOptions> options = List.of();

    /** 创建导出构造器，空数据仍输出表头与填写模板。 */
    public ExcelExportBuilder(ExcelService service, Class<T> headType, Collection<T> data) {
        Assert.notNull(headType, "Excel 表头类型不能为空");
        Assert.notNull(data, "Excel 导出数据不能为空，请使用空集合表示空表");
        this.service = service;
        this.headType = headType;
        this.rows = data::iterator;
        this.batchSize = service.getProperties().getBatchSize();
        this.dropdownRows = service.getProperties().getDropdownRows();
    }

    /** 设置工作表名称，下载文件名未单独设置时跟随此名称。 */
    public ExcelExportBuilder<T> sheetName(String value) {
        Assert.hasText(value, "工作表名称不能为空");
        WorkbookUtil.validateSheetName(value);
        this.sheetName = value;
        return this;
    }

    /** 设置下载文件名，与工作表名称独立。 */
    public ExcelExportBuilder<T> filename(String value) {
        Assert.hasText(value, "文件名不能为空");
        this.filename = value;
        return this;
    }

    /** 设置是否输出表头。 */
    public ExcelExportBuilder<T> needHead(boolean value) {
        this.needHead = value;
        return this;
    }

    /** 开启注解声明的相邻行合并，支持跨批次分组。 */
    public ExcelExportBuilder<T> merge() {
        this.merge = true;
        return this;
    }

    /** 设置是否生成下拉校验。 */
    public ExcelExportBuilder<T> dropdown(boolean value) {
        this.dropdown = value;
        return this;
    }

    /** 设置是否冻结完整表头，默认开启。 */
    public ExcelExportBuilder<T> freezeHead(boolean value) {
        this.freezeHead = value;
        return this;
    }

    /** 设置文件加密密码，ZIP 模式分别加密每个 Excel 文件。 */
    public ExcelExportBuilder<T> password(String value) {
        this.password = value;
        return this;
    }

    /** 设置额外的普通或级联选项，相同字段的显式配置优先于注解。 */
    public ExcelExportBuilder<T> options(List<DropDownOptions> value) {
        this.options = List.copyOf(value);
        return this;
    }

    /** 设置下拉校验覆盖的数据行数。 */
    public ExcelExportBuilder<T> dropdownRows(int value) {
        Assert.isTrue(value > 0 && value <= 1_048_575, "下拉行数必须在 1 至 1048575 之间");
        this.dropdownRows = value;
        return this;
    }

    /** 设置每批向工作簿写入的数据量。 */
    public ExcelExportBuilder<T> batchSize(int value) {
        Assert.isTrue(value > 0, "写入批量大小必须大于零");
        this.batchSize = value;
        return this;
    }

    /** 设置每张工作表的数据行数，超出后自动分表。 */
    public ExcelExportBuilder<T> rowsPerSheet(int value) {
        Assert.isTrue(value > 0 && value <= 1_000_000, "每表数据行数必须在 1 至 1000000 之间");
        this.rowsPerSheet = value;
        return this;
    }

    /** 设置固定列宽，未设置时按内容自动计算。 */
    public ExcelExportBuilder<T> columnWidth(int value) {
        Assert.isTrue(value > 0 && value <= 255, "列宽必须在 1 至 255 之间");
        this.columnWidth = value;
        return this;
    }

    /** 设置表头与数据行高。 */
    public ExcelExportBuilder<T> rowHeight(short head, short content) {
        Assert.isTrue(head > 0 && content > 0 && head <= 409 && content <= 409, "行高必须在 1 至 409 之间");
        this.headRowHeight = head;
        this.contentRowHeight = content;
        return this;
    }

    /** 仅导出指定字段。 */
    public ExcelExportBuilder<T> includeFields(Collection<String> value) {
        this.includeFields = List.copyOf(value);
        return this;
    }

    /** 排除指定字段。 */
    public ExcelExportBuilder<T> excludeFields(Collection<String> value) {
        this.excludeFields = List.copyOf(value);
        return this;
    }

    /** 按 includeFields 的字段顺序排列导出列。 */
    public ExcelExportBuilder<T> orderByIncludeColumn(boolean value) {
        this.orderByIncludeColumn = value;
        return this;
    }

    /** 注册自定义转换器，同类型配置覆盖内置转换器。 */
    public ExcelExportBuilder<T> registerConverter(Converter<?> converter) {
        Assert.notNull(converter, "Excel 转换器不能为空");
        converters.add(converter);
        return this;
    }

    /** 注册处理器工厂，每个 ZIP 内的工作簿获得独立实例，避免状态污染。 */
    public ExcelExportBuilder<T> registerWriteHandler(Supplier<? extends WriteHandler> factory) {
        Assert.notNull(factory, "Excel 写处理器工厂不能为空");
        handlers.add(factory);
        return this;
    }

    /** 按需查询数据页，页码从一开始，以空集合结束；建议使用稳定排序或游标分页。 */
    public ExcelExportBuilder<T> pages(IntFunction<? extends Collection<T>> provider) {
        Assert.notNull(provider, "Excel 分页数据提供者不能为空");
        this.rows = () -> new ExcelPageIterator<>(provider);
        return this;
    }

    /** 按上一批末行继续查询，首次传入 null，返回空列表结束；游标状态由模块管理。 */
    public ExcelExportBuilder<T> cursor(Function<T, ? extends List<T>> provider) {
        Assert.notNull(provider, "Excel 游标数据提供者不能为空");
        this.rows = () -> {
            AtomicReference<T> lastRow = new AtomicReference<>();
            return new ExcelPageIterator<>(pageNumber -> {
                List<T> page = provider.apply(lastRow.get());
                Assert.notNull(page, "Excel 游标数据提供者必须返回列表，结束时返回空列表");
                if (!page.isEmpty()) {
                    lastRow.set(page.getLast());
                }
                return page;
            });
        };
        return this;
    }

    /** 开启 ZIP 文件拆分，每个文件最多包含指定数据行数，空数据也输出一个 Excel。 */
    public ExcelExportBuilder<T> zip(int rowsPerFile) {
        Assert.isTrue(rowsPerFile > 0, "每个 ZIP 分片的数据行数必须大于零");
        this.zipRows = rowsPerFile;
        return this;
    }

    /** 写入调用者管理的输出流，不关闭该流。 */
    public void toStream(OutputStream output) {
        begin(output);
        service.execute(() -> {
            Iterator<T> iterator = rows.get();
            if (zipRows == null) {
                writeWorkbook(output, iterator, Long.MAX_VALUE);
            } else {
                writeZip(output, iterator);
            }
            return null;
        });
    }

    /** 通过受控会话写入不同模型的多个 Sheet，回调结束自动完成工作簿。 */
    public void toStream(OutputStream output, Consumer<ExcelWriterSession<T>> consumer) {
        Assert.isTrue(zipRows == null, "自定义多表写入不能同时配置 ZIP 分片");
        Assert.notNull(consumer, "Excel 写入回调不能为空");
        begin(output);
        service.execute(() -> {
            try (ExcelWriter writer = createWriter(output)) {
                ExcelWriterSession<T> session = new ExcelWriterSession<>(writer, headType, sheetName);
                try {
                    consumer.accept(session);
                } catch (RuntimeException exception) {
                    abortWorkbook(writer, exception);
                    throw exception;
                } finally {
                    session.finishSession();
                }
            } catch (RuntimeException exception) {
                throw new ServiceException("自定义 Excel 导出失败", exception);
            }
            return null;
        });
    }

    /** 导出到 HTTP 响应，文件名自动追加正确扩展名。 */
    public void toResponse(HttpServletResponse response) {
        ExcelResponseUtils.write(response, filename == null ? sheetName : filename, zipRows != null, this::toStream);
    }

    /** 将自定义多表写入结果直接输出到 HTTP 响应。 */
    public void toResponse(HttpServletResponse response, Consumer<ExcelWriterSession<T>> consumer) {
        Assert.isTrue(zipRows == null, "自定义多表写入不能同时配置 ZIP 分片");
        ExcelResponseUtils.write(response, filename == null ? sheetName : filename, false,
                output -> toStream(output, consumer));
    }

    /** 将小文件完整保留为字节数组，大文件应使用 toStream 或 toFile。 */
    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        toStream(output);
        return output.toByteArray();
    }

    /** 写入文件并关闭本方法创建的流，已存在的文件会被覆盖。 */
    public void toFile(Path path) {
        Assert.state(!used, "Excel 导出构造器只能执行一次");
        try (OutputStream output = Files.newOutputStream(path)) {
            toStream(output);
        } catch (IOException exception) {
            throw new ServiceException("写入 Excel 文件失败", exception);
        }
    }

    /** 校验一次性构造器与输出流。 */
    private void begin(OutputStream output) {
        Assert.notNull(output, "Excel 输出流不能为空");
        Assert.state(!used, "Excel 导出构造器只能执行一次");
        used = true;
    }

    /** 为每个工作簿创建独立转换器和处理器实例。 */
    private ExcelWriter createWriter(OutputStream output) {
        ExcelWriterBuilder builder = FesodSheet.write(output, headType)
            .autoCloseStream(false)
            .needHead(needHead)
            .registerWriteHandler(new ExcelSheetHandler(options, dropdown, dropdownRows, freezeHead));
        for (Class<?> type : List.of(Long.class, long.class, BigInteger.class, BigDecimal.class)) {
            builder.registerConverter(new ExcelNumberConverter(type, null));
        }
        builder.registerWriteHandler(
            columnWidth == null
                ? new LongestMatchColumnWidthStyleStrategy()
                : new SimpleColumnWidthStyleStrategy(columnWidth)
        );
        if (headRowHeight != null) {
            builder.registerWriteHandler(new SimpleRowHeightStyleStrategy(headRowHeight, contentRowHeight));
        }
        if (merge) {
            builder.registerWriteHandler(new CellMergeHandler());
        }
        if (password != null) {
            builder.password(password);
        }
        if (includeFields != null) {
            builder.includeColumnFieldNames(includeFields);
        }
        if (excludeFields != null) {
            builder.excludeColumnFieldNames(excludeFields);
        }
        builder.orderByIncludeColumn(orderByIncludeColumn);
        converters.forEach(builder::registerConverter);
        handlers.forEach(factory -> builder.registerWriteHandler(factory.get()));
        return builder.build();
    }

    /** 按固定批量写入当前工作簿，达到单表上限时自动创建下一张表。 */
    private void writeWorkbook(OutputStream output, Iterator<T> iterator, long maxRows) {
        try (ExcelWriter writer = createWriter(output)) {
            ExcelWriterSession<T> session = new ExcelWriterSession<>(writer, headType, sheetName);
            try {
                long written = 0;
                int sheetNumber = 0;
                int sheetRows = 0;
                do {
                    List<T> batch = new ArrayList<>();
                    int capacity = (int) Math.min(Math.min(batchSize, rowsPerSheet - sheetRows), maxRows - written);
                    while (batch.size() < capacity && iterator.hasNext()) {
                        batch.add(iterator.next());
                    }
                    String suffix = sheetNumber == 0 ? "" : "_" + (sheetNumber + 1);
                    String name = sheetName.substring(0, Math.min(sheetName.length(), 31 - suffix.length())) + suffix;
                    session.write(name, headType, batch);
                    written += batch.size();
                    sheetRows += batch.size();
                    if (sheetRows == rowsPerSheet) {
                        sheetNumber++;
                        sheetRows = 0;
                    }
                } while (written < maxRows && iterator.hasNext());
            } catch (RuntimeException exception) {
                // 数据查询失败时禁止 close 将已经写入的部分记录输出成成功文件。
                abortWorkbook(writer, exception);
                throw exception;
            } finally {
                session.finishSession();
            }
        } catch (RuntimeException exception) {
            throw new ServiceException("导出 Excel 失败", exception);
        }
    }

    /** 取消工作簿输出并清理临时文件，保留原始失败原因和清理异常。 */
    private void abortWorkbook(ExcelWriter writer, RuntimeException exception) {
        try {
            writer.writeContext().finish(true);
        } catch (RuntimeException cleanupException) {
            exception.addSuppressed(cleanupException);
        }
    }

    /** 每个 Excel 直接写入 ZIP 条目，不把整个文件转成内存字节数组。 */
    private void writeZip(OutputStream output, Iterator<T> iterator) {
        try (ZipOutputStream zip = new ZipOutputStream(StreamUtils.nonClosing(output), StandardCharsets.UTF_8)) {
            int fileNumber = 1;
            do {
                zip.putNextEntry(new ZipEntry("数据_" + fileNumber++ + ".xlsx"));
                writeWorkbook(zip, iterator, zipRows);
                zip.closeEntry();
            } while (iterator.hasNext());
            zip.finish();
        } catch (IOException exception) {
            throw new ServiceException("导出 Excel 压缩包失败", exception);
        }
    }
}
