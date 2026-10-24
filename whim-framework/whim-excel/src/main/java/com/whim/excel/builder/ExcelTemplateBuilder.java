package com.whim.excel.builder;

import com.whim.core.exception.ServiceException;
import com.whim.excel.convert.ExcelNumberConverter;
import com.whim.excel.service.ExcelService;
import com.whim.excel.utils.ExcelResponseUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.WriteDirectionEnum;
import org.apache.fesod.sheet.write.builder.ExcelWriterBuilder;
import org.apache.fesod.sheet.write.handler.WriteHandler;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.apache.fesod.sheet.write.metadata.fill.FillConfig;
import org.apache.fesod.sheet.write.metadata.fill.FillWrapper;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/10/25
 * @description XLSX 模板填充，支持普通变量、单列表、命名列表、多 Sheet 和横向填充。
 */
public final class ExcelTemplateBuilder {

    private final ExcelService service;
    private final Resource template;
    private final List<FillOperation> operations = new ArrayList<>();
    private final List<Converter<?>> converters = new ArrayList<>();
    private final List<Supplier<? extends WriteHandler>> handlers = new ArrayList<>();
    private int sheetNumber;
    private boolean forceNewRow = true;
    private boolean horizontal;
    private boolean used;
    private String filename = "模板数据";
    private String password;

    /** 创建模板构造器，模板输入流由本模块打开和关闭。 */
    public ExcelTemplateBuilder(ExcelService service, Resource template) {
        Assert.notNull(template, "Excel 模板资源不能为空");
        this.service = service;
        this.template = template;
    }

    /** 设置下载文件名。 */
    public ExcelTemplateBuilder filename(String value) {
        Assert.hasText(value, "文件名不能为空");
        this.filename = value;
        return this;
    }

    /** 设置生成文件的加密密码。 */
    public ExcelTemplateBuilder password(String value) {
        this.password = value;
        return this;
    }

    /** 选择后续填充操作对应的模板 Sheet，编号从零开始。 */
    public ExcelTemplateBuilder sheet(int value) {
        Assert.isTrue(value >= 0, "模板工作表编号不能小于零");
        this.sheetNumber = value;
        return this;
    }

    /** 设置后续列表填充是否插入新行，列表下方有内容时应开启。 */
    public ExcelTemplateBuilder forceNewRow(boolean value) {
        this.forceNewRow = value;
        return this;
    }

    /** 设置后续列表是否横向填充。 */
    public ExcelTemplateBuilder horizontal(boolean value) {
        this.horizontal = value;
        return this;
    }

    /** 填充普通对象或无前缀列表，对应 {属性} 或 {.属性}。 */
    public ExcelTemplateBuilder data(Object data) {
        Assert.notNull(data, "模板填充数据不能为空");
        operations.add(new FillOperation(sheetNumber, null, data, forceNewRow, horizontal));
        return this;
    }

    /** 填充命名列表，例如 list("users", rows) 对应 {users.name}。 */
    public ExcelTemplateBuilder list(String prefix, Collection<?> rows) {
        Assert.hasText(prefix, "模板列表前缀不能为空");
        Assert.notNull(rows, "模板列表不能为空，请使用空集合表示空列表");
        operations.add(new FillOperation(sheetNumber, prefix, rows, forceNewRow, horizontal));
        return this;
    }

    /** 填充多个命名列表与普通变量；集合值按键名填充，其他值作为普通变量。 */
    public ExcelTemplateBuilder multiList(Map<String, ?> data) {
        Assert.notNull(data, "模板填充数据不能为空");
        Map<String, Object> variables = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (value instanceof Collection<?> rows) {
                list(key, rows);
            } else {
                variables.put(key, value);
            }
        });
        if (!variables.isEmpty()) {
            data(variables);
        }
        return this;
    }

    /** 按列表顺序填充多张模板表，每张表支持多个命名列表及普通变量。 */
    public ExcelTemplateBuilder multiSheet(List<? extends Map<String, ?>> data) {
        Assert.notNull(data, "多工作表模板数据不能为空");
        for (int index = 0; index < data.size(); index++) {
            sheet(index).multiList(data.get(index));
        }
        return this;
    }

    /** 注册自定义字段转换器。 */
    public ExcelTemplateBuilder registerConverter(Converter<?> converter) {
        Assert.notNull(converter, "Excel 转换器不能为空");
        converters.add(converter);
        return this;
    }

    /** 注册模板写入处理器工厂。 */
    public ExcelTemplateBuilder registerWriteHandler(Supplier<? extends WriteHandler> factory) {
        Assert.notNull(factory, "模板写处理器工厂不能为空");
        handlers.add(factory);
        return this;
    }

    /** 写入调用者管理的流，空操作列表可用于下载原始模板。 */
    public void toStream(OutputStream output) {
        Assert.notNull(output, "Excel 输出流不能为空");
        Assert.state(!used, "Excel 模板构造器只能执行一次");
        used = true;
        service.execute(() -> {
            try (InputStream input = template.getInputStream()) {
                ExcelWriterBuilder builder = FesodSheet.write(output).withTemplate(input).autoCloseStream(false);
                for (Class<?> type : List.of(Long.class, long.class, BigInteger.class, BigDecimal.class)) {
                    builder.registerConverter(new ExcelNumberConverter(type, null));
                }
                if (password != null) {
                    builder.password(password);
                }
                converters.forEach(builder::registerConverter);
                handlers.forEach(factory -> builder.registerWriteHandler(factory.get()));
                try (ExcelWriter writer = builder.build()) {
                    Map<Integer, WriteSheet> sheets = new LinkedHashMap<>();
                    for (FillOperation operation : operations) {
                        WriteSheet sheet = sheets.computeIfAbsent(operation.sheetNumber, index ->
                            FesodSheet.writerSheet(index).build()
                        );
                        FillConfig fillConfig = FillConfig.builder()
                            .forceNewRow(operation.forceNewRow)
                            .direction(
                                operation.horizontal ? WriteDirectionEnum.HORIZONTAL : WriteDirectionEnum.VERTICAL
                            )
                            .build();
                        Object data =
                            operation.prefix == null
                                ? operation.data
                                : new FillWrapper(operation.prefix, (Collection<?>) operation.data);
                        writer.fill(data, fillConfig, sheet);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                throw new ServiceException("Excel 模板填充失败，请检查模板资源和占位符", exception);
            }
            return null;
        });
    }

    /** 将填充结果写入 HTTP 附件响应。 */
    public void toResponse(HttpServletResponse response) {
        ExcelResponseUtils.prepare(response, filename, false);
        try {
            toStream(response.getOutputStream());
        } catch (IOException exception) {
            throw new ServiceException("获取模板下载输出流失败", exception);
        }
    }

    /** 返回小型模板结果的完整字节数组。 */
    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        toStream(output);
        return output.toByteArray();
    }

    /** 将模板结果写入文件，已存在的文件会被覆盖。 */
    public void toFile(Path path) {
        Assert.state(!used, "Excel 模板构造器只能执行一次");
        try (OutputStream output = Files.newOutputStream(path)) {
            toStream(output);
        } catch (IOException exception) {
            throw new ServiceException("写入模板文件失败", exception);
        }
    }

    /**
     * @author Jince
     * @date 2026/10/25
     * @description 单次模板填充操作，保留调用时的工作表和方向配置。
     */
    @RequiredArgsConstructor
    private static final class FillOperation {

        private final int sheetNumber;
        private final String prefix;
        private final Object data;
        private final boolean forceNewRow;
        private final boolean horizontal;
    }
}
