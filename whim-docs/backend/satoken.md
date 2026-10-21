---
title: Sa-Token 权限注解
description: 系统账号登录、权限、角色和二次认证检查的真实语义。
---

# Sa-Token 权限注解

系统 API 使用 `com.whim.satoken.annotation` 下的组合注解。它们将账号体系固定为 `system`，并通过 Spring 合并注解策略交给 Sa-Token 检查。

## 登录拦截范围

当前拦截器注册到 `/**`，其路由登录规则检查 `/system/**`。未登录放行列表来自 `application.yml` 的 `sa-token.exclude-paths`，目前包含首页、验证码、登录和错误路径。

::: info 路由边界
不能将现状理解为“所有业务 API 默认都需要登录”。新增其他业务路由或账号体系时，应补充相应的登录规则，再配置必要的公开入口。
:::

## SystemCheckLogin

```java
import com.whim.satoken.annotation.SystemCheckLogin;

@SystemCheckLogin
```

无参数，支持类或方法。用于明确要求系统账号登录的 Controller。

## SystemCheckPermission

| 参数     | 类型       | 默认                             |
| -------- | ---------- | -------------------------------- |
| `value`  | `String[]` | 空数组，实际使用应提供明确权限码 |
| `mode`   | `SaMode`   | `AND`                            |
| `orRole` | `String[]` | 空数组                           |

```java
import com.whim.satoken.annotation.SystemCheckPermission;

@SystemCheckPermission("system:post:list")
```

多个权限默认全部满足；任意一个满足时使用 OR：

```java
import cn.dev33.satoken.annotation.SaMode;

@SystemCheckPermission(
    value = {"system:user:list", "system:post:list"},
    mode = SaMode.OR
)
```

`orRole` 是权限失败后的替代角色条件：数组各元素为 OR，单个元素中以逗号分隔的角色组合为 AND。这会改变接口授权含义，普通 CRUD 不应随意添加角色替代条件。

## SystemCheckRole

参数 `value: String[]` 默认空数组，`mode: SaMode` 默认 AND。

```java
import com.whim.satoken.annotation.SystemCheckRole;

@SystemCheckRole("superadmin")
```

角色检查与权限检查不同。角色名表达身份，权限码表达操作。可按业务需要明确使用，避免用宽泛角色替代所有精确操作权限。

## SystemCheckSafe

```java
import com.whim.satoken.annotation.SystemCheckSafe;

@SystemCheckSafe
```

它提供系统账号的二次认证检查标记。当前仓库没有二次认证开启、验证码确认或完成流程的现成接口。

::: warning 当前限制
`SystemCheckSafe.value` 虽有默认服务名，但尚未通过 `@AliasFor` 映射到底层注解。本站只展示无参检查，不承诺自定义服务名已经正确转发。完整二次认证业务流程需要单独实现。
:::

## 为什么组合注解能生效

权限与角色注解通过 `@AliasFor` 将参数映射到底层 Sa-Token 注解。`SaTokenConfigure.rewriteSaStrategy()` 使用 `AnnotatedElementUtils::getMergedAnnotation`，让 Sa-Token 能读取 Spring 元注解和合并后的参数。

当前执行入口为 MVC `SaInterceptor`。Service 数据范围依赖另一个 `DataPermissionAspect`，不要把二者的执行机制混为一谈。

## 与数据权限配合

Controller 检查 `system:post:list`，相应 Service 方法也以相同精确权限码声明 `@DataPermission`。前者确认可以进入操作，后者过滤岗位记录。新增写入时还需显式检查新记录归属。

两者共用登录上下文：功能权限读取 `permissionCodeSet`，数据权限按 `permissionRoleIds` 找到授予当前操作的角色，再使用 `roleInfoList` 中的范围。授权变更在事务提交后使受影响用户全部会话失效，重新登录时重建上下文。

超级管理员依据上下文中的 `superadmin` 角色识别，其有效权限在登录时加载，执行时仍检查权限集合；不通过返回通配符 `*` 放开所有接口。

## 错误返回

`SaTokenExceptionHandler` 将未登录映射为 HTTP 401，无权限 / 无角色映射为 HTTP 403，并使用统一 `Result`。客户端同时遵循 HTTP 状态与统一响应约定，不为某个端点另建错误码体系。

源码：`whim-framework/whim-satoken/src/main/java/com/whim/satoken/annotation`、`config/SaTokenConfigure.java`、`handler/SaTokenExceptionHandler.java`。
