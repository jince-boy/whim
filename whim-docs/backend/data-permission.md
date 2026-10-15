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
  → DataPermissionProvider 解析有效授权
  → 数据权限插件为 SQL 附加范围
  → 分页 / 持久化执行
```

`whim-core` 定义范围与解析抽象，`whim-mybatisplus` 执行 AOP 和 SQL 条件注入，系统业务的 `SysDataScopeService` 解析系统账号、角色与部门。

## 操作登记

权限目录中的目标权限必须存在、启用、适用，并设置 `data_permission=1`。受保护服务会验证登记；没有登记不会自动视作全部数据。

权限码采用精确操作，例如 `system:post:list`、`system:post:update`。只拥有查询权限的角色，不会为修改操作贡献数据范围。

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

真实的岗位成员查询同时保护岗位与用户：

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

用户只有一个主部门，可以关联多个角色与岗位。岗位不会隐式授予角色，也不会扩大该用户的功能授权。

## 角色默认值与操作覆盖

| 数据                     | 存储位置                         |
| ------------------------ | -------------------------------- |
| 角色默认范围             | `sys_role.data_scope`            |
| 默认自定义部门           | `sys_role_dept`                  |
| 角色在某操作上的覆盖范围 | `sys_role_permission.data_scope` |
| 操作覆盖的自定义部门     | `sys_role_permission_dept`       |

操作范围非空时覆盖角色默认值，为空时继承。新角色默认为本人范围，且没有功能授权。必须先授予精确操作，角色的数据范围才参与该操作的计算。

例如：角色 A 拥有列表操作且范围为本部门，角色 B 拥有同一列表操作且范围为本人，则列表范围为二者并集。角色 C 只有修改操作，不参与列表范围计算。

## 并集与交集

- 同一精确操作的有效角色之间取并集。
- 嵌套服务调用对同一张表的上下文取交集，内层调用不能扩大外层范围。
- 合法但为空的授权范围生成 `1=0`，结果为空；无效配置直接拒绝。
- `superadmin` 仍需要目标权限存在、启用和正确登记，不依靠 `*` 跳过全部检查。

## 新建与归属变更

SELECT、UPDATE、DELETE 可以附加 WHERE 范围。INSERT 没有旧记录 WHERE，必须显式检查新记录归属：

```java
import com.whim.mybatisplus.permission.DataPermissionContext;

// 在已经声明相同业务表的 @DataPermission 服务方法内执行。
DataPermissionContext.checkOwnership("sys_post", ownerUserId, deptId);
```

`checkOwnership(String tableName, Long ownerUserId, Long departmentId)` 检查所有当前嵌套映射。表未声明也会拒绝。当前范围可通过 `requiredScope()` 获取，不能在缺少上下文时自行构造全部范围。

归属迁移顺序为：按当前操作范围读取并锁定旧记录 → 校验新归属 → 更新。业务还要自行验证负责人存在、部门有效和审批规则。岗位服务另有更严格的本人范围约束，不等同于任意资源都能使用同样归属组合。

`createBy` 表示实际创建操作者，不能代替 `ownerUserId`。审计操作者和业务负责人可能不同。

## 事务与缓存

多表更新在 Service 使用 `@Transactional(rollbackFor = Exception.class)`。事务成功后再使业务缓存或认证会话失效，避免回滚后缓存与数据库不一致。

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

源码：`whim-mybatisplus/annotation`、`whim-mybatisplus/permission`；业务解析：`whim-system/service/impl/SysDataScopeService.java`。完整仓库说明为 `docs/rbac-data-permission.md`。
