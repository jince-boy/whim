---
title: 文件存储
description: 本地、MinIO 和阿里云 OSS 的统一调用、配置及资源管理。
---

# 文件存储

`whim-file` 提供存储基础能力，依赖 `whim-core`，由 `whim-start` 装配。它不创建上传接口、文件业务表、静态资源映射或鉴权放行规则。文件归属、业务权限与数据库记录由业务模块负责。

移植来源为 [whim2 文件模块](https://github.com/jince-boy/whim2/tree/e73e01ee84c8d6a506ce5dd8cefd089c397af627/whim-framework/whim-file)。保留统一服务、配置式调用和三类存储，重新整理输入资源、相对路径及客户端生命周期。

## 接入与默认配置

应用已引入模块，默认存入进程工作目录下的 `data/files`。生产环境通过 `FILE_STORAGE_PATH` 指定持久卷绝对路径。该目录不会自动公开为 HTTP 静态资源，且已经加入 Git 忽略规则。

业务模块需要调用服务时，在其 POM 中声明 `com.whim:whim-file`，无需填写版本。通过构造器注入 `FileStorageService`。

```yaml
file:
  storage:
    enabled: true
    default-storage: local
    default-config-name: local
    max-file-size: 20MB
    connect-timeout: 10s
    read-timeout: 30s
    request-timeout: 5m
    local:
      - name: local
        base-path: ${FILE_STORAGE_PATH:./data/files}
```

`enabled: false` 关闭自动配置。默认平台必须有可用配置；`default-config-name` 不配置时使用默认平台的第一项。每个平台内的配置名称必须唯一。启动时校验配置和本地目录，不主动探测云存储网络或创建桶。

## 云存储配置

桶应预先创建。可在同一平台下配置多个账号或桶；`base-path` 是相对对象前缀，可为空，不以 `/` 开头或结尾。

```yaml
file:
  storage:
    default-storage: minio
    default-config-name: assets
    minio:
      - name: assets
        url: ${MINIO_ENDPOINT}
        access-key: ${MINIO_ACCESS_KEY}
        secret-key: ${MINIO_SECRET_KEY}
        bucket: ${MINIO_BUCKET}
        region: us-east-1
        base-path: assets
    aliyun-oss:
      - name: archive
        url: ${OSS_ENDPOINT}
        access-key: ${OSS_ACCESS_KEY}
        secret-key: ${OSS_SECRET_KEY}
        bucket: ${OSS_BUCKET}
        region: ${OSS_REGION}
        base-path: archive
```

OSS 必须指定桶所在地域，使用 V4 签名。Endpoint 应使用完整的 HTTP 或 HTTPS 地址，生产环境使用 HTTPS。签名地址中的主机沿用 Endpoint；浏览器直传时应选择客户端可访问的地址，并配置桶 CORS。

每套配置复用一个 SDK 客户端和连接池，应用关闭时统一清理。上传、读取信息、检查存在、删除均不会为每次操作新建客户端。

## 上传

```java
MetaData metadata = fileStorageService.upload(multipartFile,
        options -> options.storagePath("avatars"));

MetaData archive = fileStorageService.upload(bytes,
        options -> options.platform("aliyunOss")
                .platformConfigName("archive")
                .objectKey("reports/2026/report.pdf")
                .contentType("application/pdf"));
```

支持 `MultipartFile`、`Path`、`File`、`byte[]`、`InputStream` 和受限制的 `URI`。默认文件名使用 `IdUtils.uuid32()`，保留有效扩展名，无有效扩展名时使用 `.bin`。显式指定相同 key 会替换原对象。

`objectKey` 与 `storagePath`、`fileName` 两种定位方式不可混用。`fileName` 不允许包含目录；目录使用 `/`，拒绝 `..`、反斜杠、绝对路径及 Windows 歧义文件名。本地根目录使用原生 `Path`，支持 Windows 和 Linux。

服务打开文件输入后负责关闭，传入的 `InputStream` 也会关闭。调用配置函数失败或找不到目标存储时尚未接管输入流。若上传成功后关闭输入失败，方法会抛出异常，但对象可能已写入；应根据确定的 key 核查，避免不加判断地重试。

服务同时校验声明大小和实际读取大小，未知长度流不能绕过上限。允许配置的单文件上限为大于零至 5 GiB；HTTP 上传还受 `spring.servlet.multipart` 限制。传输时间检查在读取前后进行，自定义阻塞 `InputStream` 自身也必须支持超时或取消。

本地上传使用同目录临时文件和原子替换；文件系统不支持原子移动时明确失败，清理临时文件并保留已有目标。存储目录应仅允许应用进程写入；路径和符号链接检查不能替代操作系统权限隔离。

## 文件身份与元数据

| 字段 | 含义 |
| --- | --- |
| `platform` | `local`、`minio`、`aliyunOss` 或自定义平台 |
| `platformConfigName` | 平台内配置名称 |
| `objectKey` | 相对配置根目录或对象前缀的完整 key |
| `storagePath` | 相对目录，不包含文件名 |
| `fileName` | 存储文件名 |
| `fileSize` | 字节数 |
| `contentType` | 内容类型 |

业务记录至少保存 `platform + platformConfigName + objectKey`。例如 MinIO 的 `base-path: assets` 和返回的 `objectKey: avatars/a.png` 对应桶内 `assets/avatars/a.png`。再次操作时传回 `avatars/a.png`，不要重复添加前缀。

本地实现不会返回服务器绝对路径，也不单独持久化 MIME 信息：上传结果使用本次声明的内容类型，后续查询通过文件名和操作系统探测，不可识别时使用 `application/octet-stream`。客户端 MIME 声明不能替代内容检查、业务允许类型校验或病毒检测。

存储配置名称和根目录/前缀是文件身份的一部分。已有文件入库后，应保持它们稳定；改变根目录或桶需要业务侧执行实际数据迁移。

## 下载、查询与删除

```java
try (DownloadHandler download = fileStorageService.download(metadata)) {
    download.writeToOutputStream(outputStream);
}

boolean exists = fileStorageService.exists(options -> options
        .platform(metadata.getPlatform())
        .platformConfigName(metadata.getPlatformConfigName())
        .objectKey(metadata.getObjectKey()));

MetaData current = fileStorageService.getFileMetaData(options -> options
        .platform(metadata.getPlatform())
        .platformConfigName(metadata.getPlatformConfigName())
        .objectKey(metadata.getObjectKey()));

fileStorageService.deleteFile(metadata);
```

`DownloadHandler` 是单次消费资源。`writeToOutputStream`、`consumeInputStream`、`toByteArray` 会自动释放输入流，输出流仍归调用方所有。直接调用 `getInputStream()` 时必须关闭该流或处理器；不要多次消费同一个处理器。

`toByteArray()` 默认最多读取 20 MiB，可通过 `toByteArray(int maximumBytes)` 显式指定上限；大文件使用流式输出。

`exists()` 仅在对象不存在时返回 `false`；认证失败、桶配置错误及远端故障抛出 `FileStorageException`。删除是幂等操作，对象不存在也视为成功；云存储启用版本控制时遵循提供商的删除标记语义，不清除历史版本。异常复用项目已有全局映射。

## 预签名地址

```java
String downloadUrl = fileStorageService.getFilePreSignedUrl(options -> options
        .platform("minio").platformConfigName("assets")
        .objectKey("reports/report.pdf"), Duration.ofMinutes(10));

String uploadUrl = fileStorageService.uploadFilePreSignedUrl(options -> options
        .platform("aliyunOss").platformConfigName("archive")
        .objectKey("reports/new.pdf").contentType("application/pdf"), Duration.ofMinutes(5));
```

上传签名使用 HTTP PUT，并要求请求使用相同的 `Content-Type`。未指定时为 `application/octet-stream`。有效期为 1 秒至 7 天的整数秒，仍支持原来的 `Integer + TimeUnit` 重载。

签名保持 SDK 原始编码，不做 URL 解码。生成下载签名不会提前检查对象存在；本地存储不支持签名，调用时抛出明确异常。

直传绕过应用读取流程，`max-file-size` 不限制直传上传。需要由桶策略或上传完成后的业务校验约束大小和类型，再确认文件归属及业务记录。签名应在授权后生成，不能把任意 key 的签名能力直接开放给用户。

## 远程 URI 上传

默认 `remote-allowed-hosts` 为空，禁止 URI 上传。仅在需要从受信任服务抓取时明确启用：

```yaml
file:
  storage:
    remote-allowed-hosts:
      - trusted-assets.example.com
```

```java
MetaData imported = fileStorageService.upload(
        URI.create("https://trusted-assets.example.com/images/photo.png"),
        options -> options.storagePath("imports"));
```

只接受白名单中的精确主机名及 HTTP/HTTPS 默认端口，不支持 `file:`、用户凭据、URL fragment、通配符或重定向。元信息与正文使用同一连接，配置连接/读取超时，并按上传上限计数。白名单应仅包含管理员信任且控制访问范围的域名；内网隔离还应通过网络出口策略约束。

## 自定义存储

实现 `IFileStorage` 并注册为 Spring Bean 即可加入统一服务，无需修改平台配置类或使用反射。也可继承 `BaseFileStorage` 复用实例身份和元数据构造。

自定义实例需提供唯一的平台与配置名称，遵循相对 key、异常及幂等删除语义，并实现幂等 `close()`。服务及 Spring 可能分别执行关闭。自定义 `FileStorageService` Bean 可以覆盖默认服务。

## 与旧模块的差异

- 保留 `upload(file, options -> ...)` 等主要调用方式；增加 `Duration` 和元数据定位重载。
- 元数据路径统一为相对路径，旧记录中的本地绝对路径或包含桶前缀的 key 不能直接当作新 key 使用。
- 输入适配器和包装器合并为 `FileSource`，去掉接口到基类的强制转换；本地路径额外支持 `Path`。
- 移除反射配置读取、请求级客户端工厂及未使用的 Tika Bean；使用 MinIO 9.0.3、阿里云 OSS V2 SDK 0.6.1。OSS 使用默认的 Apache HttpClient 5 传输实现，排除旧 HttpClient 4，不引入 JAXB 依赖组合。
- 配置错误提前在启动时暴露；删除改为幂等；远程 URI 默认关闭；内存下载有明确上限。

本次仅移植框架能力，没有新增或修改 HTTP API、数据库结构及授权规则。
