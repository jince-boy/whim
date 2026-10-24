package com.whim.config;

import com.whim.excel.core.ExcelDictionaryProvider;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 在应用装配层连接 Excel 字典接口与系统字典缓存查询。
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ExcelDictionaryConfiguration {

    private final ISysDictDataService dictionaryService;

    /** 注册系统字典适配器，保留业务服务返回的选项排序。 */
    @Bean
    @ConditionalOnMissingBean(ExcelDictionaryProvider.class)
    public ExcelDictionaryProvider excelDictionaryProvider() {
        return dictionaryType -> {
            Map<String, String> values = new LinkedHashMap<>();
            for (SysDictDataVO item : dictionaryService.getDictDataByType(dictionaryType)) {
                if (values.putIfAbsent(item.getValue(), item.getLabel()) != null) {
                    throw new IllegalArgumentException("系统字典存在重复编码：" + dictionaryType);
                }
            }
            return values;
        };
    }
}
