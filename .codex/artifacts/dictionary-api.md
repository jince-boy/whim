# 字典接口说明

配套文件：[OpenAPI 文档](system-dictionary.openapi.json)，包含 14 个字典接口，可导入 Apifox。当前会话没有可用的 Apifox MCP 连接，尚未同步在线项目。

所有接口要求系统账号登录。字典是系统共享配置，不按部门或创建人隔离；管理接口校验各自的功能权限。

认证请求头为 `Authorization: Bearer <token>`，令牌由系统登录接口返回。

| 方法 | 路由 | 权限码 |
| --- | --- | --- |
| GET | `/system/dictType/page` | `system:dictType:page` |
| GET | `/system/dictType/detail?id=...` | `system:dictType:detail` |
| GET | `/system/dictType/type/{type}` | `system:dictType:detail` |
| GET | `/system/dictType/list` | `system:dictType:page` |
| POST | `/system/dictType/add` | `system:dictType:add` |
| PUT | `/system/dictType/update` | `system:dictType:update` |
| DELETE | `/system/dictType/delete?dictTypeIds=101,102` | `system:dictType:delete` |
| DELETE | `/system/dictType/reset` | `system:dictType:reset` |
| GET | `/system/dictData/page` | `system:dictData:page` |
| GET | `/system/dictData/detail?id=...` | `system:dictData:detail` |
| POST | `/system/dictData/add` | `system:dictData:add` |
| PUT | `/system/dictData/update` | `system:dictData:update` |
| DELETE | `/system/dictData/delete?dictDataIds=201,202` | `system:dictData:delete` |
| GET | `/system/dictData/type/{type}` | 仅登录，无额外管理权限 |

`V1__initialize.sql` 是唯一初始化迁移，面向空数据库，包含全部业务表、约束、校验触发器、初始数据及字典菜单权限，并为内置超级管理员补齐授权关系。

字典类型与字典数据均无状态字段。字典数据的请求、响应、实体及数据库字段统一使用 `type` 编码关联，建表时直接采用最终结构。

## 参数和响应

分页参数为 `pageNum`、`pageSize`，默认 `1`、`10`，每页最多 `100` 条。类型分页支持名称 `name`、编码 `type` 精确查询；数据分页必须提供 `type`，并支持标签 `label` 精确查询。

类型编码最长 128 个字符，以字母开头，只能包含字母、数字、下划线。类型名称最长 64 个字符，标签和值最长 128 个字符，备注最长 255 个字符。

新增类型请求：

```json
{"name":"业务状态","type":"business_status","remark":"业务表单共用"}
```

新增数据请求：

```json
{"type":"business_status","label":"正常","value":"0","listClass":"success","sort":0,"remark":""}
```

修改接口提交相同业务字段并增加 `id`。修改采用完整编辑语义；备注省略或为 `null` 时清空。数据项的样式默认为 `default`，排序默认为 `0`，修改时也遵循这些默认值。

样式只允许 `default`、`primary`、`info`、`success`、`warning`、`error`。排序不能小于 `0`。批量删除接受逗号分隔或重复查询参数，单次 1 至 100 个正整数 ID；字典数据直接按传入 ID 批量删除，不预先检查每个 ID 是否存在，重复 ID 不影响删除，部分 ID 不存在不阻止其余记录删除；没有任何记录被删除时抛出 `ServiceException`。字典类型删除按查询到的现存类型处理。

普通接口返回 `Result<T>`，成功 `code` 为 `200`，写入成功时 `data` 为 `null`。分页数据包含 `currentPage`、`data`、`pages`、`size`、`total`。ID 超出 JavaScript 安全整数范围时响应为字符串，客户端应保留原值。

## 业务规则

字典数据统一使用 `type` 编码关联。类型编码变化时，在同一事务内同步未删除数据项的编码，保留数据项备注并填充更新审计字段；已删除记录保留历史编码。数据项编辑不能迁移到其他类型。类型存在未删除的数据项时禁止删除。字典类型新增和修改使用事务；服务通过 MyBatis-Plus 条件构造器操作，不拼接锁或查询 SQL。

删除采用 `deleted = id`，未删除值为 `0`，联合唯一索引允许业务编码或字典值在删除后再次创建。客户端不能写入审计字段或删除标识。

管理分页、详情和类型列表查询未删除记录；类型详情查询或修改时记录不存在会抛出 `ServiceException`，按现有全局异常处理返回 `500`。数据分页没有匹配记录时返回空分页。选项接口返回指定类型下未删除的数据，按 `sort`、`id` 升序；类型不存在或已删除时返回空数组。

缓存统一使用 `CacheKeys.SYS_DICT_TYPE`（`sys_dict_type`）和 `CacheKeys.SYS_DICT`（`sys_dict`），缓存键使用传入的编码原值，不进行大小写转换，不额外设置过期时间。类型查询不缓存不存在的结果，选项查询缓存空列表；新增类型时写入空选项。类型新增、修改通过 `@CachePut` 更新选项，修改返回的数据按 `sort`、`id` 升序，同时清理旧编码的类型信息及选项缓存；数据新增、修改使对应类型的选项缓存失效；数据批量删除成功后通过 `@CacheEvict(allEntries = true)` 清空全部字典选项缓存，后续查询重新加载。缓存操作经过现有事务感知缓存管理器，事务回滚不更新缓存。重置接口只清除这两类缓存，不查询数据库，不预加载数据；后续查询通过 `@Cacheable` 重新加载。

字典类型和字典数据的导出接口、前端入口及原有导出工具均已移除。

请求参数校验失败返回 `400`；字典类型和字典数据的业务错误统一抛出 `ServiceException`，按现有全局处理返回 `500`，包括记录不存在、数据项变更所属类型和写入失败。未登录为 `401`，无权限为 `403`，唯一约束冲突为 `409`，其他异常为 `500`。错误响应复用项目全局处理器与 `Result` 格式，HTTP 状态与 `code` 一致。
