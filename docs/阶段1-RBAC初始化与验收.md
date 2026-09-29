# 阶段 1：RBAC 初始化与验收

> 对应 [任务计划](任务计划.md) 的阶段 1。依次应用 `V3__rbac_scope_and_member_status.sql`、`V4__rbac_permission_catalog.sql` 和 `V5__initialize_admin.sql`。V5 负责初始化平台管理员；随后 V6、V7 为尚无租户的安装补齐组织字段和[默认租户](默认租户与可选多租户.md)。

## 迁移规则

- 平台角色：`sys_role.tenant_id IS NULL`、`role_type = 0`；其用户绑定和权限绑定的 `tenant_id` 也为空。
- 租户角色：`sys_role.tenant_id`、`sys_user_role.tenant_id`、`sys_role_permission.tenant_id` 均为同一个真实租户 ID，`role_type = 1`。
- 迁移会将旧的平台绑定改为空租户，移除同一用户与角色、同一角色与权限的冗余绑定，并移除错租户的历史关系。被移除的原始行分别保存在 `sys_rbac_migration_user_role_archive`、`sys_rbac_migration_role_permission_archive`，便于管理员审查。迁移之后不得再把租户 ID 写进平台角色绑定。
- 现有成员关系回填为启用状态 `status = 0`。停用成员无法取得租户角色和套餐权限。
- `V4` 只补缺少的功能权限码，不自动授予任意角色或套餐。新建的其他租户仍需配置套餐与租户角色；V7 对首次安装的默认租户做一次初始授权。

## 首位平台管理员

`V5__initialize_admin.sql` 在数据库中不存在用户名为 `admin` 的账号时，自动创建启用状态的 `admin` 用户、有效的平台 `superadmin` 角色及全局角色绑定。初始密码是字面值 **`admin123...`**，末尾包含三个英文句点；数据库中仅保存与 `BCryptUtils` 兼容的 BCrypt 强度 10 哈希。用户、角色和绑定 ID 在迁移时从各自表的当前最大 ID 递增取得。

已有 `admin` 账号时，V5 不修改其密码、状态或角色绑定；它也不会重置已初始化数据库中的密码。默认密码仅适用于新创建的 `admin`，正式环境首次登录后应尽快改为独立密码。该初始化没有公开 HTTP 入口。

迁移后可用以下只读查询核对账号和平台角色绑定：

```sql
SELECT u.id, u.username, u.status, r.role_code, r.role_type,
       r.tenant_id AS role_tenant_id, ur.tenant_id AS binding_tenant_id
FROM sys_user u
         INNER JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
         INNER JOIN sys_role r ON r.id = ur.role_id AND r.status = 0 AND r.deleted = 0
WHERE u.username = 'admin' AND u.deleted = 0;
```

新初始化的结果应包含一条全局 `superadmin` 绑定，角色和绑定的租户字段均为 `NULL`。

## 权限与 API

RBAC 是这一阶段的功能权限模型，不对应统一的 Controller 或管理 Service。HTTP 入口按用户、角色、权限、租户四个业务主体组织；关联关系通过主体的子资源表达，DTO/VO 也归入主体目录：

| 业务主体 | 管理入口 |
| --- | --- |
| 用户 | `/system/user/{userId}/status`、`/system/user/{userId}/roles` |
| 角色 | `/system/role`、`/system/role/{roleId}`、`/system/role/{roleId}/status`、`/system/role/{roleId}/permissions` |
| 权限 | `/system/permission`、`/system/permission/platform`、`/system/permission/{permissionId}/status` |
| 租户 | `/system/tenant/{tenantId}/status`、`/system/tenant/{tenantId}/package`、`/system/tenant/current/members`、`/system/tenant/current/members/{userId}/status`、`/system/tenant/packages/{packageId}/status`、`/system/tenant/packages/{packageId}/permissions` |

成员、角色及其关系操作以当前令牌的 `currentTenantId` 为范围；当前租户缺失或不可用时返回 403。每个入口都有独立的 `@SystemCheckPermission` 权限码。平台操作除入口权限码外，还在所属业务服务中要求全局 `superadmin` 角色。平台接口的目标租户 ID 只用于明确的跨租户平台操作，不改变普通租户接口的范围。

租户角色只能分配当前租户套餐内、当前启用的权限。角色与套餐的交集在登录和切换租户时重新查询；用户、成员、租户、套餐、角色或权限失效时不产生有效权限。所有通过本阶段管理入口执行的授权和状态变更，在事务提交成功后踢出受影响用户的旧会话；回滚时不踢出。超级管理员的 `*` 仅用于功能入口校验；租户业务仍必须显式选择有效租户。

本阶段允许管理**已有成员**的状态和角色。新用户加入租户的邀请、确认与撤销仍按任务计划阶段 5 实现，不能通过任意提交用户 ID 绕过该流程。开发验收所需的初始成员关系由受控数据准备建立。

## 两租户验收清单

按[阶段 0 数据规格](阶段0-技术基线与访问边界.md)建立租户 A、B、不同套餐、同一用户在两个租户内的不同角色，以及至少一个未包含在套餐内的角色权限。依次核对：

1. 未登录调用 `/system/tenant/current/members` 得到 401；已登录但缺少 `system:member:list` 得到 403。
2. 同一用户切到 A 和 B 后，角色和权限集合分别只含对应租户的有效授权；无当前租户时无法进入租户管理接口。
3. 角色权限表中存在、套餐中不存在的权限不能通过功能校验；停用套餐或成员后也不能取得该租户授权。
4. 用 A 的令牌指定 B 的角色或成员 ID 进行维护，应得到 403，且 B 的绑定不变。
5. 修改角色、角色权限、成员状态或套餐后，旧令牌被踢下线；模拟数据库回滚时旧令牌不应因失败操作被踢下线。
6. 超级管理员必须先选定有效租户才能调用租户接口；普通租户接口不能因 `*` 读取其他租户成员。

本地测试库已通过应用启动执行 Flyway V5，初始管理员已核对。两租户真实 HTTP 动态测试、事务回滚和会话失效的逐项结果见[阶段 1 动态验收报告](阶段1-RBAC动态验收报告.md)。83 项检查均已通过，本阶段验收完成。
