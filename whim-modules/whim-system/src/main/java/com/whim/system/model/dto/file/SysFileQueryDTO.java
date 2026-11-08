package com.whim.system.model.dto.file;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件分页查询。
 */
@Getter
@Setter
public class SysFileQueryDTO extends PageQueryDTO {
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String originalName;
    private Long storageId;
}
