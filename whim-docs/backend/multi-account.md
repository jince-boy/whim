---
title: 多账号认证
description: 当前 SYSTEM 实现、集中注册机制与新账号体系的扩展步骤。
---

# 多账号认证

多账号体系用于隔离系统用户、会员、商家等不同身份。Whim 已有集中注册和分派入口，当前实际仅注册 `SYSTEM`，没有现成会员或商家登录接口。

## 先区分三个概念

| 概念                     | 当前状态                          |
| ------------------------ | --------------------------------- |
| 同一账号多个终端同时在线 | 已支持，当前配置最多 3 个登录会话 |
| 多个独立账号体系         | 框架预留；仅系统账号已经落地      |
| 多租户隔离或租户切换     | 当前未实现，不等同于多账号        |

## 统一注册入口

`AuthUserType` 当前定义 `SYSTEM = "system"`。`StpAuthManager` 为它创建 `StpLogicJwtForSimple`：

```java
public static final StpLogic SYSTEM = new StpLogicJwtForSimple(AuthUserType.SYSTEM);

/** 返回需要注册的账号体系。 */
public static List<StpLogic> allRegistered() {
    return List.of(SYSTEM);
}
```

`SaTokenConfigure` 将集合中的体系统一注册。`getStpLogic(loginType)` 按类型查找，未注册时抛出中文错误；`getCurrentStpLogic()` 逐个检查登录状态并返回第一个匹配体系；`isLogin()` 判断是否有已登录体系。

## 开发接入示例：增加会员体系

以下步骤是新增能力的开发指引，不表示仓库已经包含会员模块。

### 1. 增加类型与注册

在 core 的 `AuthUserType` 添加 `MEMBER = "member"`。在认证模块的 `StpAuthManager` 添加相应实例，并更新现有集合：

```java
public static final StpLogic MEMBER = new StpLogicJwtForSimple(AuthUserType.MEMBER);

/** 同时注册系统与会员账号体系。 */
public static List<StpLogic> allRegistered() {
    return List.of(SYSTEM, MEMBER);
}
```

这是现有类内的修改片段，导入沿用 `cn.dev33.satoken.stp.StpLogic` 与 `cn.dev33.satoken.jwt.StpLogicJwtForSimple`。

### 2. 实现业务登录

在对应业务模块验证会员账号和凭证，构建有效 `UserInfo`，设置 `loginType = AuthUserType.MEMBER`，通过注入的 `AuthenticationSession.login(userInfo, rememberMe)` 创建会话。

不能直接把前端提交的角色和权限填入 `UserInfo`。授权快照应来自该账号体系可信的服务端授权模型。

### 3. 定义体系专属注解

以下为新增 `MemberCheckPermission.java` 的完整示例；前提是第一步已经加入 `AuthUserType.MEMBER`：

```java
package com.whim.satoken.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.whim.core.auth.constants.AuthUserType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/10/15
 * @description 会员账号精确操作权限检查。
 */
@SaCheckPermission(type = AuthUserType.MEMBER)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MemberCheckPermission {
    /** 需要检查的权限码。 */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    /** 多权限匹配模式。 */
    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;
}
```

登录和角色注解也应固定正确的账号 type。不要在会员接口上复用固定为 SYSTEM 的系统组合注解。

### 4. 保护路由区域

在认证配置中为 `/member/**` 等新区域添加对应体系的登录检查，必要的公开登录、验证码端点加入 YAML 放行列表。当前 `/system/**` 登录规则不会自动保护新区域。

### 5. 明确令牌来源与当前体系

当前 `getCurrentStpLogic()` 返回注册顺序中的第一个已登录体系，没有同一次请求同时携带多体系凭证时的显式选择策略。新增体系时需要设计清晰的令牌来源、路由约束和识别策略，不能依赖注册顺序表达业务身份。

### 6. 接入数据权限

如果会员资源需要记录级保护，还需为该体系提供数据权限解析或分派。当前 `SysDataScopeService` 查询的是系统用户与系统角色表，加常量不会自动获得另一体系的数据授权。

## 业务层保持抽象

日常业务注入 `AuthenticationContext` 和 `AuthenticationSession`。具体 `StpLogic`、体系注册与 Sa-Token 注解组合留在认证框架中。这样可以扩展认证实现，而不把框架 API 扩散到业务模块。

源码：`whim-core/auth/constants/AuthUserType.java`、`whim-satoken/security/StpAuthManager.java`、`whim-satoken/session/SaTokenAuthenticationSession.java`。
