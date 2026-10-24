package com.whim.excel.config;

import com.whim.excel.config.properties.ExcelProperties;
import com.whim.excel.core.ExcelDictionaryProvider;
import com.whim.excel.service.ExcelService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.fesod.sheet.FesodSheet;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 自动配置，复用应用的 Validator 并按需接入业务字典。
 */
@AutoConfiguration
@ConditionalOnClass(FesodSheet.class)
@EnableConfigurationProperties(ExcelProperties.class)
@RequiredArgsConstructor
public class ExcelAutoConfiguration {

    private final ExcelProperties properties;

    /** 注册统一 Excel 服务，自定义服务 Bean 可替换默认实现。 */
    @Bean
    @ConditionalOnMissingBean
    public ExcelService excelService(
        Validator validator,
        ObjectProvider<ExcelDictionaryProvider> dictionaryProvider,
        ListableBeanFactory beanFactory
    ) {
        return new ExcelService(properties, validator, dictionaryProvider, beanFactory);
    }
}
