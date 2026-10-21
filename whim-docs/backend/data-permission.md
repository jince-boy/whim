---
title: 数据权限
description: 精确操作授权、五种数据范围、SQL 过滤与归属校验。
---

# 数据权限

功能权限决定“能否执行此操作”，数据权限决定“此操作可以访问哪些记录”。Controller 的权限检查与 Service 的数据范围声明需要配合使用。

## 执行链路

```text
请求携带 Bearer Token
  → Sa-Token 校验登录与 Controller 功能权限
  → Service @DataPermission 声明精确操作与表映射
  → DataPermissionProvider 从登录上下文解析角色范围
  → 数据权限插件为 SQL 附加范围
  → 分页 / 持久化执行
```

`whim-core` 定义范围与解析抽象，`whim-mybatisplus` 执行 AOP 和 SQL 条件注入，系统业务的 `SysDataScopeServiceImpl` 读取登录上下文并按需加载部门范围。

## 操作登记

权限目录中的目标权限必须是存在且启用的页面或按钮权限。登录或刷新用户信息时一次性加载有效授权，写入 `UserInfo.permissionRoleIds`，并从这份映射生成 `permissionCodeSet`。数据权限执行时直接检查登录上下文，不重新查询用户、权限或角色授权关系。

服务方法标注 `@DataPermission` 即建立数据权限上下文，权限表没有额外的数据过滤开关。未标注的方法本身不建立上下文；如果它由受保护的方法调用，内部 SQL 仍然沿用外层范围。

权限码采用精确操作，例如 `system:post:list`、`system:post:update`。只拥有查询权限的角色，不会为修改操作贡献数据范围。

上下文保存 `permissionRoleIds`（权限码到授权角色ID集合）和带数据范围的 `roleInfoList`。解析器先按当前操作选出角色，再合并范围，不能把用户拥有的全部角色直接合并。旧会话缺少对应关系时拒绝访问，需要重新登录或刷新用户信息。

## 注解与表映射

```java
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;

// 标记公开的岗位列表服务方法。
@DataPermission(
    permission = "system:post:list",
    tables = @DataPermissionTable(name = "sys_post")
)
```

`@DataPermission` 的 `permission`、`tables` 均必填，目标为方法。`@DataPermissionTable` 是嵌套映射：

| 属性               | 默认            | 说明                                     |
| ------------------ | --------------- | ---------------------------------------- |
| `name`             | 必填            | 真实数据库表名                           |
| `alias`            | 空字符串        | 空时匹配该表全部别名；非空只匹配指定别名 |
| `departmentColumn` | `dept_id`       | 部门归属列                               |
| `userColumn`       | `owner_user_id` | 业务归属人列                             |

用户表本人维度映射到 `id`：

```java
@DataPermission(
    permission = "system:user:list",
    tables = @DataPermissionTable(name = "sys_user", userColumn = "id")
)
```

涉及岗位与用户的成员查询，可以同时声明两张表：

```java
@DataPermission(
    permission = "system:post:member:list",
    tables = {
        @DataPermissionTable(name = "sys_post"),
        @DataPermissionTable(name = "sys_user", userColumn = "id")
    }
)
```

表名、别名与列名由开发者静态声明，只接受普通标识符，不接受请求输入和 SQL 表达式。不支持的归属列可以设为空字符串，但部门列与用户列不能同时为空。

## 五种数据范围

| 编码 | 范围         | 规则                             |
| ---- | ------------ | -------------------------------- |
| 1    | 全部         | 此操作可访问全部业务记录         |
| 2    | 自定义部门   | 指定的有效部门，不自动包含下级   |
| 3    | 本部门       | 当前有效用户的主部门             |
| 4    | 本部门及下级 | 从有效主部门沿真实有效部门树递归 |
| 5    | 本人         | 映射的业务归属人等于当前用户 ID  |

用户只有一个主部门，可以关联多个角色与岗位。岗位不会隐式授予角色，也不会扩大该用户的功能授权。全部范围直接返回，纯本人范围不查询部门；需要部门范围时，批量加载自定义部门并校验候选部门及祖先链，再按需展开本部门的下级。

## 角色范围与多角色组合

| 数据 | 存储位置 |
| --- | --- |
| 角色统一的数据范围 | `sys_role.data_scope` |
| 角色的自定义部门 | `sys_role_dept` |
| 角色授予的功能权限 | `sys_role_permission` |

一个角色只配置一套范围，角色授予的所有操作共用这套范围。不同操作需要不同范围时，拆成不同角色再分配给用户。角色与权限的关联只负责功能授权，不设置范围覆盖。新角色默认为本人范围，且没有功能授权。

只有授予当前精确操作的有效角色才参与范围合并。例如，给同一用户分配“员工查看员”和“销售员工编辑员”：

| 角色 | 功能权限 | 数据范围 |
| --- | --- | --- |
| 员工查看员 | `system:user:list` | 全部 |
| 销售员工编辑员 | `system:user:update` | 自定义部门：销售部 |

该用户能查看全部员工，但只能修改销售部员工。查看员角色没有修改权限，所以其全部范围不参与修改操作。如果多个角色都授予同一操作，则合并这些角色的范围，例如“本部门”和“本人”合并为“本部门或本人”。

## 并集与交集

- 同一精确操作的有效角色之间取并集。
- 嵌套服务调用对同一张表的上下文取交集，内层调用不能扩大外层范围。
- 合法但为空的授权范围生成 `1=0`，结果为空；无效配置直接拒绝。
- `superadmin` 的全部有效权限在登录时载入上下文，执行时仍检查当前操作是否在权限集合内，不返回通配符 `*`。

## 新建与归属变更

SELECT、UPDATE、DELETE 可以附加 WHERE 范围。INSERT 没有旧记录 WHERE，必须显式检查新记录归属：

```java
import com.whim.mybatisplus.permission.DataPermissionContext;

// 在已经声明相同业务表的 @DataPermission 服务方法内执行。
DataPermissionContext.checkOwnership("sys_post", ownerUserId, deptId);
```

`checkOwnership(String tableName, Long ownerUserId, Long departmentId)` 检查所有当前嵌套映射。表未声明也会拒绝。当前范围可通过 `requiredScope()` 获取，不能在缺少上下文时自行构造全部范围。

归属迁移顺序为：按当前操作范围读取并锁定旧记录 → 校验新归属 → 更新。业务还要自行验证负责人存在、部门有效和审批规则。

`createBy` 表示实际创建操作者，不能代替 `ownerUserId`。审计操作者和业务负责人可能不同。

## 事务与缓存

多表更新在 Service 使用 `@Transactional(rollbackFor = Exception.class)`。事务成功后再使业务缓存或认证会话失效，避免回滚后缓存与数据库不一致。

功能权限与数据权限共用登录快照。用户角色、角色权限、角色范围或账号有效性改变后，使用已有的事务提交后踢出机制，让受影响用户的全部会话失效；重新登录后使用最新上下文。数据权限解析不承担授权变更后的逐次查库校验。

Mapper 共享二级缓存不能跨范围复用结果；业务缓存必须考虑授权范围，并在授权变化后正确失效。不能只按业务记录 ID 缓存已经裁剪过的列表结果。

## 内部元数据查询

`@DataPermissionMetadata` 仅用于内部授权解析和完整性查询，强制只读 SELECT。不能用它包装写操作，也不能把它当作业务接口的免鉴权开关。

`@InterceptorIgnore(dataPermission="true")` 或线程忽略策略不能关闭受保护业务范围，框架会拒绝。

## 接入检查

| 检查点      | 需要满足                                           |
| ----------- | -------------------------------------------------- |
| 调用入口    | 可信且已经认证授权                                 |
| Spring 代理 | 从代理调用公开注解方法；同类自调用不会重新触发注解 |
| 多表 SQL    | 声明每张需要保护的真实业务表，包括关联与子查询     |
| 写入        | 新建、归属迁移显式检查新归属                       |
| 异步任务    | 不假设 ThreadLocal 自动跨线程传播                  |
| 分页        | 范围注入发生在分页之前                             |

定时任务、线程池和非 HTTP 入口仍需可信身份与明确授权流程，不能把公开 Service 当作免鉴权入口。

源码：`whim-mybatisplus/annotation`、`whim-mybatisplus/permission`；业务解析：`whim-system/service/impl/SysDataScopeServiceImpl.java`。完整仓库说明为 `docs/rbac-data-permission.md`。
