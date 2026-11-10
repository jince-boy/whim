package com.whim.system.model.dto.file;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件分页查询。
 */
@Getter
@Setter
public class SysFileQueryDTO extends PageQueryDTO {
    @Size(max = 512, message = "文件名称不能超过512个字符")
    private String fileName;
    @Size(max = 255, message = "原文件名称不能超过255个字符")
    private String originalName;
    @Size(max = 255, message = "文件后缀不能超过255个字符")
    private String fileSuffix;
    private Long storageId;
    @Size(max = 32, message = "存储类型不能超过32个字符")
    private String platform;
    @Size(max = 64, message = "上传途径不能超过64个字符")
    private String uploadSource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
