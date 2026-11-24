package com.whim.system.model.dto.homepage;

import com.whim.system.model.entity.SysHomePage;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 修改首页配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysHomePage.class, reverseConvertGenerate = false)
public class SysHomePageUpdateDTO extends SysHomePageAddDTO {
    @NotNull(message = "首页配置ID不能为空")
    private Long id;
}
