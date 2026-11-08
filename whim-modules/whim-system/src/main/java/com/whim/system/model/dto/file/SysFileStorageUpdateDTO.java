package com.whim.system.model.dto.file;

import com.whim.system.model.entity.SysFileStorage;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 修改存储配置，目标 ID 与字段在同一请求中。
 */
@Getter
@Setter
@AutoMapper(target = SysFileStorage.class, reverseConvertGenerate = false)
public class SysFileStorageUpdateDTO extends SysFileStorageAddDTO {
    @NotNull(message = "配置ID不能为空")
    private Long id;
}
