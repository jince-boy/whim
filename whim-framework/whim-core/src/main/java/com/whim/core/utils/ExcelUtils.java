package com.whim.core.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.whim.core.exception.ExcelException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jince
 * date: 2025/8/15 17:29
 * description: Excel工具类 基于阿里巴巴 EasyExcel 封装，提供导入、单 Sheet 导出、多 Sheet 导出等功能
 */
public class ExcelUtils {
    /**
     * 导出 Excel（带数据）
     *
     * @param data  导出数据
     * @param clazz 数据模型类（与表头对应）
     * @return 导出构建器
     */
    public static <T> ExportBuilder<T> exportExcel(List<T> data, Class<T> clazz) {
        return new ExportBuilder<>(data, clazz);
    }

    /**
     * 导出 Excel 模板（只包含表头，无数据）
     *
     * @param clazz 数据模型类（与表头对应）
     * @return 导出构建器
     */
    public static <T> ExportBuilder<T> exportTemplate(Class<T> clazz) {
        return new ExportBuilder<>(Collections.emptyList(), clazz);
    }

    /**
     * 多 Sheet 导出
     *
     * @param fileName 导出文件名
     * @return 多 Sheet 导出构建器
     */
    public static MultiSheetExportBuilder multiSheetExport(String fileName) {
        return new MultiSheetExportBuilder(fileName);
    }

    /**
     * 导入 Excel
     *
     * @param inputStream 输入流
     * @param clazz       数据模型类
     * @param listener    每页数据处理回调（分页读取避免内存溢出）
     * @param <T>         数据类型
     */
    public static <T> void importExcel(InputStream inputStream, Class<T> clazz, Consumer<List<T>> listener) {
        try {
            EasyExcel.read(inputStream, clazz, new PageReadListener<>(listener))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            throw new ExcelException("Excel 导入失败", e);
        }
    }


    /**
     * 单 Sheet 导出构建器
     *
     * @param <T> 数据类型
     */
    @RequiredArgsConstructor
    public static class ExportBuilder<T> {
        private final List<T> data;                           // 导出数据
        private final Class<T> clazz;                         // 数据模型类
        private String sheetName = "Sheet1";                  // 默认 sheet 名
        private String fileName = "export.xlsx";              // 默认导出文件名
        private Consumer<ExcelWriterSheetBuilder> sheetCustomizer; // sheet 自定义配置回调
        private final List<WriteHandler> writeHandlers = new ArrayList<>(); // 写处理器（如自动列宽）

        /**
         * 设置 sheet 名称
         */
        public ExportBuilder<T> sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        /**
         * 设置文件名（自动补全 .xlsx 后缀）
         */
        public ExportBuilder<T> fileName(String fileName) {
            this.fileName = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
            return this;
        }

        /**
         * 自定义 sheet 构建器（如设置样式、冻结行列等）
         */
        public ExportBuilder<T> customize(Consumer<ExcelWriterSheetBuilder> customizer) {
            this.sheetCustomizer = customizer;
            return this;
        }

        /**
         * 自动设置列宽
         */
        public ExportBuilder<T> autoColumnWidth() {
            this.writeHandlers.add(new LongestMatchColumnWidthStyleStrategy());
            return this;
        }

        /**
         * 注册自定义写处理器
         */
        public ExportBuilder<T> registerWriteHandler(WriteHandler handler) {
            this.writeHandlers.add(handler);
            return this;
        }

        /**
         * 导出到 HttpServletResponse（前端下载）
         */
        public void toResponse(HttpServletResponse response) {
            try (ServletOutputStream os = prepareResponse(fileName, response)) {
                toStream(os);
            } catch (IOException e) {
                throw new ExcelException("Excel 导出失败", e);
            }
        }

        /**
         * 导出到输出流（可写入文件、OSS、MinIO 等）
         */
        public void toStream(OutputStream os) {
            ExcelWriterBuilder writerBuilder = EasyExcel.write(os, clazz)
                    .autoCloseStream(false);

            // 注册写处理器（如自动列宽、样式处理）
            writeHandlers.forEach(writerBuilder::registerWriteHandler);

            // 构建 sheet
            ExcelWriterSheetBuilder sheetBuilder = writerBuilder.sheet(sheetName);
            if (sheetCustomizer != null) {
                sheetCustomizer.accept(sheetBuilder);
            }

            // 写入数据
            sheetBuilder.doWrite(data);
        }
    }

    /**
     * 多 Sheet 导出构建器
     */
    public static class MultiSheetExportBuilder {
        private final String fileName;                // 导出文件名
        private final List<SheetData<?>> sheets = new ArrayList<>(); // sheet 集合

        public MultiSheetExportBuilder(String fileName) {
            this.fileName = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
        }

        /**
         * 添加一个 sheet
         *
         * @param sheetName  sheet 名
         * @param data       数据
         * @param clazz      数据模型类
         * @param customizer 自定义 sheet 构建器
         */
        public <T> MultiSheetExportBuilder addSheet(String sheetName, List<T> data, Class<T> clazz,
                                                    Consumer<ExcelWriterSheetBuilder> customizer) {
            sheets.add(new SheetData<>(sheetName, data, clazz, customizer));
            return this;
        }

        /**
         * 导出到 HttpServletResponse
         */
        public void toResponse(HttpServletResponse response) {
            try (ServletOutputStream os = prepareResponse(fileName, response)) {
                toStream(os);
            } catch (IOException e) {
                throw new ExcelException("多Sheet Excel 导出失败", e);
            }
        }

        /**
         * 导出到输出流
         */
        public void toStream(OutputStream os) {
            try (ExcelWriter build = EasyExcel.write(os).autoCloseStream(false).build()) {
                for (int i = 0; i < sheets.size(); i++) {
                    SheetData<?> sheetData = sheets.get(i);

                    // 构建 sheet
                    ExcelWriterSheetBuilder sheetBuilder = EasyExcel.writerSheet(i, sheetData.sheetName)
                            .head(sheetData.clazz);

                    if (sheetData.customizer != null) {
                        sheetData.customizer.accept(sheetBuilder);
                    }

                    // 写入数据
                    sheetBuilder.doWrite(sheetData.data);
                }
            }
        }

        /**
         * 封装 sheet 元数据
         */
        private static class SheetData<T> {
            private final String sheetName;
            private final List<T> data;
            private final Class<T> clazz;
            private final Consumer<ExcelWriterSheetBuilder> customizer;

            private SheetData(String sheetName, List<T> data, Class<T> clazz,
                              Consumer<ExcelWriterSheetBuilder> customizer) {
                this.sheetName = sheetName;
                this.data = data;
                this.clazz = clazz;
                this.customizer = customizer;
            }
        }
    }

    /**
     * 设置响应头，支持中文文件名，防止乱码
     *
     * @param fileName 导出文件名
     * @param response HttpServletResponse
     */
    private static ServletOutputStream prepareResponse(String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 通过 URLEncoder.encode 防止中文乱码
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encoded);
        return response.getOutputStream();
    }

}
