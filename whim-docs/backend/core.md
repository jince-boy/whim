---
title: 核心工具与 Web 约定
description: 公共工具、统一响应、分页、异常和审计字段。
---

# 核心工具与 Web 约定

公共能力按模块分工。工具与认证抽象位于 `whim-core`；统一响应和 HTTP 异常处理属于 `whim-web`；分页与实体审计位于 `whim-mybatisplus`。

## 现有工具优先复用

工具目录为 `whim-framework/whim-core/src/main/java/com/whim/core/utils`。使用前核对方法契约与重载，不为不同语义强行套用现有工具。

| 工具                   | 已提供的能力                                |
| ---------------------- | ------------------------------------------- |
| `AmountUtils`          | 金额空值、正负、舍入、元分、解析与大写      |
| `BCryptUtils`          | 密码编码与验证                              |
| `BeanConvertUtils`     | Bean、列表与 Map 转换                       |
| `DateUtils`            | 日期格式、解析、运算、日边界与类型转换      |
| `DesensitizationUtils` | 按保留位置或类型脱敏                        |
| `IdUtils`              | UUID 与 32 字符 UUID                        |
| `IPUtils`              | 客户端 IP 与归属地                          |
| `RandomStringUtils`    | 字母、数字、字母数字与自定义字符随机串      |
| `RestClientUtils`      | RestClient 常见请求方法与可配置选项         |
| `ServletUtils`         | 当前请求、头、参数、分页参数、URL 编码与 UA |
| `SpringUtils`          | 上下文、Bean、环境与代理访问                |
| `ValueParserUtils`     | 标量、枚举、分隔数组及全半角转换            |

Spring Bean 优先构造器注入，只有无法直接注入时才使用 `SpringUtils`。真正缺少的共享逻辑放回合适的已有工具，不在业务模块重复建同名工具体系。

### HTTP 请求

同步业务请求使用已有的 `RestClientUtils`，支持 `get/post/put/patch/delete`、泛型响应、请求头和查询参数，统一抛出 `HttpException`。底层为 Spring `RestClient` 与 JDK `HttpClient`，无需引入 WebFlux 或 OkHttp。

```java
String response = RestClientUtils.get("https://example.com/api/status", String.class);
```

需要独立连接设置或 SDK 原始响应处理时，使用 `RestClientUtils.createRestClient(httpClient, requestTimeout)`。该方法不修改工具类的全局实例；调用方负责 JDK 客户端的生命周期。`requestTimeout` 限制整个 HTTP 交换及响应流消费时间，不是逐次读取的空闲超时。SDK 已签名的地址使用 `uri(URI)` 保留编码，流式响应使用 `exchange` 并明确由谁关闭响应。

## 统一响应

`com.whim.web.model.Result<T>` 包含 `code`、`message`、`data`，由 Web 层统一保持 HTTP 状态与响应码一致。

```java
import com.whim.web.model.Result;

Result.success("保存成功");
Result.success("查询成功", data);
Result.unauthorized("请先登录");
Result.permissionDenied("没有操作权限");
```

以上为方法使用片段，`data` 为当前接口返回值。需要资源文件时使用已有文件响应能力，不把下载包装成普通 JSON。

## 分页契约

请求使用 `PageQueryDTO`，响应使用 `PageDataVO<T>`。表格前端接收 `Result<PageDataVO<T>>` 对应的外层响应：

| 分页字段      | 含义       |
| ------------- | ---------- |
| `data`        | 当前页数据 |
| `total`       | 总记录数   |
| `currentPage` | 当前页     |
| `size`        | 每页数量   |
| `pages`       | 总页数     |

接口层请求参数与 DTO 校验保持一致，不自行再发明 `records`、`rows` 等并行契约。关联查询若使用数据权限，要保证过滤发生在分页与计数之前。

## 异常

`GlobalExceptionHandler` 统一处理 Web 异常。业务优先复用已有异常，如 `ServiceException`、`DataAccessDeniedException`、`UserNotFoundException`、`UserDisableException`、`UserPasswordNotMatchException`、`TooManyRequestsException`、`HttpException` 等。

日志与业务异常使用中文。不要返回只有 `Result.code` 变化而 HTTP 状态错误的响应，不为单个接口引入另一套状态码格式。

## 参数校验与 XSS

DTO 使用 `jakarta.validation`，Controller 对请求使用 `@Valid` 等校验入口。全局 XSS 过滤是请求清洗的一层，不能替代字段业务校验、授权检查和正确的输出编码。

富文本确需跳过过滤时，了解 `@XssIgnore` 的[边界说明](/backend/annotations#xssignore)，并实现业务白名单处理。

## 审计与业务归属

`BaseEntity` 与 `AutoFillFieldHandler` 协作维护 `createBy/createTime/updateBy/updateTime/deleteBy/deleteTime`。填充器通过 `AuthenticationContext` 获取操作者，持久化模块不直接依赖 Sa-Token。

审计描述谁执行了操作。数据权限中的 `owner_user_id` 描述记录属于谁，`dept_id` 描述所属部门，不能互相替代。新增与归属变更仍需 `DataPermissionContext.checkOwnership`。

## 配置与迁移

默认应用端口为 `8089`；认证排除路径来自 YAML。Flyway 位于 `whim-start/src/main/resources/db/migration`，每次数据库变化都创建新的更高版本迁移。不要修改、删除或复用已应用脚本。

源码：`whim-core/utils`、`whim-core/exception`、`whim-web/model/Result.java`、`whim-mybatisplus/model` 与 `handler/AutoFillFieldHandler.java`。
