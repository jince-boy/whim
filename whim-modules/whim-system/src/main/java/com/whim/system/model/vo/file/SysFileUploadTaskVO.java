package com.whim.system.model.vo.file;

import com.whim.system.model.entity.SysFileUploadTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 上传任务进度，只返回存储端确认的分片，不暴露存储会话和密钥。
 */
@Getter
@Setter
@AutoMapper(target = SysFileUploadTask.class, convertGenerate = false)
public class SysFileUploadTaskVO {
    private Long id;
    private Integer partSize;
    private Long fileSize;
    private List<Integer> uploadedParts;
    private Long fileId;
    private Boolean cancelled;
    private LocalDateTime expiresAt;
}
