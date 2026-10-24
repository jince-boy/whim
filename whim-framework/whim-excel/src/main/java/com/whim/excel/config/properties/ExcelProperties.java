package com.whim.excel.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 默认处理参数，可在单次操作中覆盖。
 */
@Data
@Validated
@ConfigurationProperties("whim.excel")
public class ExcelProperties {

    /** 单次导入允许处理的最大数据行数，所有工作表累计。 */
    @Min(1)
    private long maxImportRows = 100_000;

    /** 达到错误行数上限后终止，防止错误结果无限增长。 */
    @Min(1)
    private int maxErrors = 100;

    /** 分批读写时每批的数据量。 */
    @Min(1)
    @Max(100_000)
    private int batchSize = 1_000;

    /** 模板下拉校验覆盖的数据行数，不包含表头。 */
    @Min(1)
    @Max(1_048_575)
    private int dropdownRows = 1_000;
}
