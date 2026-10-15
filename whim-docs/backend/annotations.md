---
title: 自定义注解
description: 仓库八个项目注解的参数、执行位置和使用边界。
---

# 自定义注解

当前项目包含八个自定义注解。以下为已实现清单，不包含框架原生注解，也不把限流、重复提交或操作审计等未实现的组合注解列为现有能力。

## 一览

| 注解                      | 模块               | 目标     | 用途                      |
| ------------------------- | ------------------ | -------- | ------------------------- |
| `@DataPermission`         | `whim-mybatisplus` | 方法     | 声明精确操作的数据范围    |
| `@DataPermissionTable`    | `whim-mybatisplus` | 嵌套注解 | 声明业务表与归属列        |
| `@DataPermissionMetadata` | `whim-mybatisplus` | 方法     | 内部只读授权 / 完整性查询 |
| `@SystemCheckLogin`       | `whim-satoken`     | 类、方法 | 系统账号登录检查          |
| `@SystemCheckPermission`  | `whim-satoken`     | 类、方法 | 系统账号功能权限检查      |
| `@SystemCheckRole`        | `whim-satoken`     | 类、方法 | 系统角色检查              |
| `@SystemCheckSafe`        | `whim-satoken`     | 类、方法 | 系统二次认证检查标记      |
| `@XssIgnore`              | `whim-web`         | 类、方法 | 跳过当前 XSS 请求过滤器   |

## 数据权限注解

`@DataPermission(permission, tables)` 参数必填。表映射支持 `name`、`alias`、`departmentColumn`、`userColumn`，默认归属列为 `dept_id` 与 `owner_user_id`。

```java
@DataPermission(
    permission = "system:post:update",
    tables = @DataPermissionTable(name = "sys_post")
)
```

放在公开 Service 方法上，经 Spring 代理执行。它限制记录范围，不能取代入口功能权限。详细规则见[数据权限](/backend/data-permission)。

`@DataPermissionMetadata` 无参数，专供内部只读查询。即使没有业务范围上下文，也不允许借该注解执行写操作。

## 系统账号组合注解

四个注解固定使用 `AuthUserType.SYSTEM`。系统接口使用项目组合注解，避免各处重复填写原始 Sa-Token 的账号 type。

```java
@SystemCheckLogin
@SystemCheckPermission("system:post:list")
@SystemCheckRole("superadmin")
@SystemCheckSafe
```

以上是分别展示的四种用法，不表示普通接口需要全部叠加。权限与角色的 AND、OR 和参数继承见[Sa-Token 权限注解](/backend/satoken)。

当前这些注解通过 MVC `SaInterceptor` 执行，不能因此声称任意普通 Service Bean 的直接调用都会自动完成注解授权。

## XssIgnore

包名：`com.whim.web.annotation.XssIgnore`。类或方法上使用，无参数，跳过全局 XSS 过滤器。

```java
import com.whim.web.annotation.XssIgnore;

@XssIgnore
```

默认请求包装器清洗请求参数和 JSON 文本节点，使用纯文本策略，不是富文本白名单；multipart 请求自动跳过。富文本接口如确需跳过，应由该业务采用专门的白名单清洗与安全输出策略。

此注解仅表示“跳过当前过滤器”，不表示输入已安全。当前 JSON 清洗异常会记录日志并回退原始 body，不能将其描述为任何异常都阻断请求的防线。

## 注解该放在哪里

| 需求                          | 位置                           |
| ----------------------------- | ------------------------------ |
| HTTP 功能权限、角色、登录检查 | Controller                     |
| 记录级操作范围                | 公开 Service 方法              |
| 数据表 / 别名 / 归属列        | `DataPermission.tables`        |
| 内部授权元数据查询            | 经审核的内部只读方法           |
| 特定接口跳过 XSS 请求过滤     | 明确需要的 Controller 类或方法 |

源码分别位于 `com.whim.mybatisplus.annotation`、`com.whim.satoken.annotation`、`com.whim.web.annotation`。
