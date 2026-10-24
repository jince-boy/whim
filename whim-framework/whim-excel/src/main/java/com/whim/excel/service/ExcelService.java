package com.whim.excel.service;

import com.whim.excel.builder.ExcelExportBuilder;
import com.whim.excel.builder.ExcelReadBuilder;
import com.whim.excel.builder.ExcelTemplateBuilder;
import com.whim.excel.config.properties.ExcelProperties;
import com.whim.excel.core.ExcelDictionaryProvider;
import com.whim.excel.core.ExcelOperationContext;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 可注入的 Excel 入口，每次读写创建独立上下文与构造器。
 */
@RequiredArgsConstructor
public class ExcelService {

    @Getter
    private final ExcelProperties properties;

    @Getter
    private final Validator validator;

    private final ObjectProvider<ExcelDictionaryProvider> dictionaryProvider;
    private final ListableBeanFactory beanFactory;

    /** 创建集合导出构造器。 */
    public <T> ExcelExportBuilder<T> of(Collection<T> rows, Class<T> headType) {
        return new ExcelExportBuilder<>(this, headType, rows);
    }

    /** 创建自定义多表写入或分页导出构造器。 */
    public <T> ExcelExportBuilder<T> writer(Class<T> headType) {
        return of(List.of(), headType);
    }

    /** 创建导入构造器，输入流由调用者管理。 */
    public <T> ExcelReadBuilder<T> read(InputStream input, Class<T> headType) {
        return new ExcelReadBuilder<>(this, input, headType);
    }

    /** 通过类路径资源创建模板构造器。 */
    public ExcelTemplateBuilder template(String classpath) {
        return template(new ClassPathResource(classpath));
    }

    /** 通过 Spring Resource 创建模板构造器，支持类路径和本地资源。 */
    public ExcelTemplateBuilder template(Resource resource) {
        return new ExcelTemplateBuilder(this, resource);
    }

    /** 在本次操作的隔离上下文中同步执行读写，不跨线程传递 writer。 */
    public <T> T execute(Supplier<T> action) {
        return new ExcelOperationContext(dictionaryProvider.getIfAvailable(), beanFactory).execute(action);
    }
}
