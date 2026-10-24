package com.whim.excel.utils;

import com.whim.core.utils.SpringUtils;
import com.whim.excel.builder.ExcelExportBuilder;
import com.whim.excel.builder.ExcelReadBuilder;
import com.whim.excel.builder.ExcelTemplateBuilder;
import com.whim.excel.service.ExcelService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 简洁调用门面，Spring Bean 内也可通过构造器注入 ExcelService。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelBuilder {

    /** 创建集合导出构造器。 */
    public static <T> ExcelExportBuilder<T> of(Collection<T> rows, Class<T> headType) {
        return SpringUtils.getBean(ExcelService.class).of(rows, headType);
    }

    /** 创建分页或自定义多表写入构造器。 */
    public static <T> ExcelExportBuilder<T> writer(Class<T> headType) {
        return SpringUtils.getBean(ExcelService.class).writer(headType);
    }

    /** 创建统一校验的导入构造器。 */
    public static <T> ExcelReadBuilder<T> read(InputStream input, Class<T> headType) {
        return SpringUtils.getBean(ExcelService.class).read(input, headType);
    }

    /** 创建类路径模板填充构造器。 */
    public static ExcelTemplateBuilder template(String classpath) {
        return SpringUtils.getBean(ExcelService.class).template(classpath);
    }

    /** 创建指定资源的模板填充构造器。 */
    public static ExcelTemplateBuilder template(Resource resource) {
        return SpringUtils.getBean(ExcelService.class).template(resource);
    }
}
