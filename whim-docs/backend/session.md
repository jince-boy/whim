---
title: 会话与授权变更
description: 认证抽象、令牌配置、权限快照和事务提交后的会话撤销。
---

# 会话与授权变更

认证上下文回答“当前用户是谁”，认证会话负责“登录、更新和退出”。两个业务抽象都位于 `whim-core`，具体实现由 `whim-satoken` 提供。

## AuthenticationContext

| 方法                     | 说明                      |
| ------------------------ | ------------------------- |
| `getCurrentUserInfo()`   | 当前有效认证用户          |
| `isLogin()`              | 当前请求是否已登录        |
| `isSuperAdministrator()` | 是否具有超级管理员身份    |
| `getUserId()`            | 默认方法，读取当前用户 ID |

其他身份字段通过 `getCurrentUserInfo()` 获取。当前接口没有 `getTenantId()`、`getDeptId()` 等便捷方法，不应从历史文档复制这些调用。

`UserInfo` 包含 `userId`、`username`、`name`、`avatar`、`deptId`、`loginType`、`permissionCodeSet`、`permissionRoleIds`、`roleCodeSet`、`roleInfoList`。`permissionRoleIds` 保存精确权限码与授权角色ID集合的对应关系，`permissionCodeSet` 由其键集合生成。业务应使用这些抽象，不直接读取 Sa-Token Session 键。

## AuthenticationSession

| 方法                                                             | 说明                                       |
| ---------------------------------------------------------------- | ------------------------------------------ |
| `login(UserInfo)`                                                | 按配置创建登录会话，返回 `AuthenticationToken` |
| `updateUserInfo(UserInfo)`                                       | 更新当前令牌的授权上下文，校验登录 ID 一致 |
| `logout()`                                                       | 注销当前会话                               |
| `kickout(String loginType, Collection<Long> userIds)`            | 撤销指定体系下这些用户的全部会话           |
| `kickoutAfterCommit(String loginType, Collection<Long> userIds)` | 事务成功提交后撤销，无实际事务时立即执行   |

`AuthenticationToken` 的字段为 `tokenType`、`accessToken`、`expiresIn`，有效期单位是秒。

## 登录和鉴权链路

登录服务验证用户后构建 `UserInfo`，会话实现按 `loginType` 选择已注册的 `StpLogic`，并把完整用户上下文写入当前 Token-Session。

客户端请求头：

```http
Authorization: Bearer <accessToken>
```

`StpInterfaceImpl` 从当前 Token 对应会话读取权限与角色，同时匹配用户 ID。它不是每次直接查询业务数据库的权限加载器。当前用户信息端点会重建有效用户信息，并刷新当前令牌的授权快照。

数据权限使用同一份登录快照，根据 `permissionRoleIds` 筛选 `roleInfoList` 中授予当前操作的角色，再合并其数据范围；仅按需查询部门数据。授权变更通过事务提交后的会话失效流程生效，不在每次解析范围时重新查询账号、权限和角色授权。新增映射仅保存在服务端上下文，不改变用户信息接口的响应结构；旧会话可重新登录或调用用户信息接口补齐映射。

## 当前关键配置

```yaml
sa-token:
  token-name: Authorization
  token-prefix: Bearer
  timeout: 604800
  active-timeout: 86400
  is-concurrent: true
  is-share: false
  max-login-count: 3
  overflowLogoutMode: REPLACED
  is-read-header: true
  is-read-cookie: false
  is-read-body: false
  exclude-paths:
    - /
    - /system/auth/captcha
    - /system/auth/login
    - /error
```

配置以 `whim-start/src/main/resources/application.yml` 的最终值为准。这里省略密钥等不应复制到公开文档的配置。

## 记住我与令牌期限

“保持登录”仅由前端控制：勾选时将令牌保存到 `localStorage`，未勾选时保存到 `sessionStorage`。登录接口不接收 `rememberMe`，所有令牌统一遵循 Sa-Token 的 `timeout` 和 `active-timeout` 配置。

`is-share: false` 表示并发登录不共享同一令牌。当前最多三个会话，超出使用 REPLACED 策略。不能将“记住我”描述为永久登录。

## 授权变更与事务

用户状态、密码、主部门或授权变化可能使已有令牌中的权限快照过期。相应业务方法通过会话撤销让旧身份重新认证。

事务写入使用 `kickoutAfterCommit`：提交成功后撤销，回滚不撤销；没有实际事务时立即执行。不要在数据库提交前撤销会话，再让事务失败后留下不一致的用户体验。

自定义业务修改授权时，也要沿用这一提交后协调方式，不能只更新授权表而长期保留旧会话。

## JWT 与服务端状态

当前使用 `StpLogicJwtForSimple`，仍依赖 Token-Session 与会话撤销机制，不是完全无状态的 JWT 方案。不要据此绕过服务端用户状态和授权有效性验证。

源码：`whim-core/auth`、`whim-satoken/context/AuthContext.java`、`session/SaTokenAuthenticationSession.java`、`service/StpInterfaceImpl.java`。
