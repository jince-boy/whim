package com.whim.file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 分片直传地址及必须原样发送的请求头。
 */
@Getter
@AllArgsConstructor
public class PartUploadSignature {
    private final String url;
    private final Map<String, String> headers;
}

