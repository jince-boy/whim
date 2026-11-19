package com.whim.system.model.vo.operlog;

import com.whim.system.model.entity.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 包含请求、响应摘要和错误信息的操作日志详情。
 */
@Getter
@Setter
@AutoMapper(target = SysOperLog.class, convertGenerate = false)
public class SysOperLogDetailVO extends SysOperLogVO {
    private String methodName;
    private String requestMethod;
    private String requestUrl;
    private String requestParam;
    private String responseParam;
    private String errorMessage;
}
