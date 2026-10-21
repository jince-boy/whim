# 单组织 RBAC 与数据权限

## 当前保留范围

后端仅保留 `AuthController` 的四个认证接口。用户、角色、部门、岗位、菜单和配置管理接口，以及角色分配、菜单导航和数据范围配置接口均已移除。接口契约见 [openapi.json](openapi.json)。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | /system/auth/captcha | 获取与客户端地址绑定的 GIF 验证码 |
| POST | /system/auth/login | 校验验证码和账号密码，建立登录会话 |
| POST | /system/auth/logout | 注销当前登录会话 |
| GET | /system/auth/userInfo | 获取当前用户信息并刷新角色、权限快照 |

验证码与登录接口允许匿名访问；退出和用户信息接口需要有效登录。登录不接收组织上下文，用户信息接口不接收切换参数。

`whim-system` 保留以下服务及其必要的 Mapper、XML 和模型：

| 服务 | 职责 |
| --- | --- |
| IAuthService / AuthServiceImpl | 验证码、登录、退出、当前用户信息 |
| ISysUserService / SysUserServiceImpl | 查询登录账号、有效账号，构建角色和权限信息 |
| ISysRoleService / SysRoleServiceImpl | 查询账号的有效角色列表 |
| ISysPermissionService / SysPermissionServiceImpl | 按权限码查询有效权限定义，构建登录角色的权限对应关系 |
| ISysDataScopeService / SysDataScopeServiceImpl | 解析当前操作的数据范围 |

部门和角色自定义部门的 Mapper 由数据权限解析直接使用。对应的管理服务已经删除。XML 仅保留登录授权和数据范围解析需要的查询。

数据库中的账号、角色、权限、部门和授权关系继续用于认证与数据权限解析。原有前端管理页面及导航接口调用尚未对应可用的后端管理接口。

## 登录与功能权限

账号在整个系统中唯一，用户名按小写查询。每个用户有一个主部门，可持有多个角色。角色授予功能权限及数据范围；岗位关系不授予角色。

登录及刷新用户信息时，`SysUserServiceImpl.buildUserInfo` 查询有效角色，并通过 `ISysPermissionService.getPermissionRoleIds` 一次性加载角色授予的有效权限，填充 `UserInfo.roleInfoList`、`roleCodeSet` 和 `permissionRoleIds`。`permissionRoleIds` 保存“精确权限码 → 授权角色ID集合”，`permissionCodeSet` 直接取其键集合。认证会话实现将这些信息保存到 Sa-Token 会话；角色授权关系仅供服务端使用，不增加用户信息接口的响应字段。

`StpInterfaceImpl.getRoleList` 和 `getPermissionList` 从会话读取角色编码及权限码，供 Sa-Token 检查。超级管理员通过有效的 `superadmin` 角色识别；其权限集合包含所有启用的页面和按钮权限，不返回通配符 `*`。

功能权限检查由 `@SystemCheckPermission` 完成，固定角色检查使用 `@SystemCheckRole`。业务代码通过 `AuthenticationContext` 获取登录身份和管理员状态，不直接依赖 Sa-Token 实现。

## 数据范围解析

框架位于 `whim-mybatisplus`，解析契约 `DataPermissionProvider` 位于 `whim-core`。`SysDataScopeServiceImpl` 实现该契约，通过 `AuthenticationContext.getCurrentUserInfo` 读取登录上下文中的用户、主部门、角色及范围，不再查询用户表、权限表或角色授权关系。只有需要部门范围时才使用部门相关 Mapper。

解析时先检查上下文的 `permissionCodeSet` 是否包含当前操作，再根据 `permissionRoleIds` 筛选 `roleInfoList` 中授予该操作的角色。只有这些角色参与范围合并，同一操作的多个角色范围取并集。登录上下文缺少对应关系时拒绝访问，不回退为全部角色；旧会话可以重新登录或调用用户信息接口重建完整上下文。

| 编码 | 范围 | 规则 |
| --- | --- | --- |
| 1 | 全部 | 允许全部业务数据 |
| 2 | 自定义部门 | 仅所选有效部门，不自动包含下级 |
| 3 | 本部门 | 按当前有效账号的主部门计算 |
| 4 | 本部门及下级 | 按真实父子关系递归计算有效部门 |
| 5 | 本人 | 按业务归属人列判断，不按创建人判断 |

一个角色只配置一套数据范围，存储在 `sys_role.data_scope`，自定义部门存储在 `sys_role_dept`。`sys_role_permission` 只维护角色授予哪些功能权限，不配置数据范围。同一角色授予的所有操作共用该角色范围，不同操作需要不同范围时通过多个角色组合。

例如，同一用户持有以下两个角色：

| 角色 | 授予的权限 | 角色范围 |
| --- | --- | --- |
| 员工查看员 | `system:user:list` | 全部 |
| 销售员工编辑员 | `system:user:update` | 自定义部门：销售部 |

查询员工时只计算查看员角色，能查看全部员工；修改员工时只计算编辑员角色，只能修改销售部员工。查看员的全部范围不会带到修改操作。如果再授予一个拥有修改权限、范围为本人的角色，则修改范围为“销售部或本人”。没有授予当前操作权限的角色，即使配置了全部范围，也不参与计算。

范围合并直接收集角色允许的部门，“本部门”和“本部门及下级”加入账号主部门，自定义部门按角色集合批量加载。候选部门沿真实父子关系向上校验，只有完整祖先链启用、未删除且到达根节点的部门有效；断链、循环和超过 500 字符路径限制的节点不产生授权。本部门及下级仅在主部门有效时向下查询。范围合并阶段最多三次部门查询，不按角色数或祖先层级逐次查询；全部范围直接返回，纯本人范围不查询部门。

是否建立数据权限上下文由服务方法上的 `@DataPermission` 决定，权限表不再配置额外开关。未登录、上下文缺少操作授权或无法解析角色范围时拒绝访问。有效但为空的数据范围保持为空，不回退为全部。未标注的方法本身不建立上下文，但如果从受保护的方法中调用，仍然沿用外层范围。

功能权限与数据权限统一使用同一份登录授权快照，超级管理员也从上下文识别。用户角色、角色权限、角色范围或账号有效性变更，遵守事务提交后让受影响用户全部会话失效的流程；重新登录时加载最新授权。`AuthenticationSession.kickoutAfterCommit` 提供现有的会话失效能力，数据权限解析不再通过逐次查库重复核验。

## 数据权限接入

在权限目录登记精确权限码并授予相应角色后，通过 Spring 代理调用声明了对应操作的公开服务方法，即启用该方法的数据过滤：

```java
@DataPermission(permission = "business:order:list", tables =
        @DataPermissionTable(name = "business_order",
                departmentColumn = "dept_id", userColumn = "owner_user_id"))
```

表名、列名和别名必须是开发者声明的静态标识符，不接收请求内容。数组可声明多张表；`alias` 为空时匹配该表的全部 SQL 别名。没有部门归属时将 `departmentColumn` 设为空，没有用户归属时将 `userColumn` 设为空；两列不能同时为空。连接和子查询需要声明每张受保护业务表及正确归属。

`DataPermissionInterceptor` 复用 MyBatis-Plus 原生数据权限插件。`DataPermissionSqlHandler` 生成部门与本人条件，注解切面建立本次操作的范围上下文。数据权限拦截器位于分页插件之前，使分页总数与结果使用同一范围。查询、更新和逻辑删除经过同一过滤机制。

新增和归属变更还需要校验新归属：

```java
DataPermissionContext.checkOwnership("business_order", ownerUserId, deptId);
```

该检查遵守所有嵌套调用范围，同一表的嵌套 SQL 条件取交集。同类内部调用不会重新触发注解，内部方法沿用已有上下文；异步和定时任务不会继承请求身份。

授权解析所用的只读 Mapper 方法声明 `@DataPermissionMetadata`，避免被当前业务范围截断。该标记只支持方法级只读查询，不能用于普通业务接口或写入。受保护业务不得通过 `@InterceptorIgnore` 或线程级忽略策略关闭过滤，也不得通过共享二级缓存复用其他范围的查询结果。

认证和持久化框架只依赖 `whim-core` 的契约，`whim-satoken` 与 `whim-mybatisplus` 不互相依赖，由 `whim-start` 组装。

## 数据库与响应约定

本次数据权限简化按重建数据库的方式直接修正 `V1__initialize.sql`：角色权限关联不再存储范围，取消单独的操作部门范围表，权限定义移除数据过滤开关。需要在空库重新执行现有 Flyway 迁移，不提供旧配置转换脚本。重建后的数据库仍包含全局账号、角色权限和部门范围等数据；后续变更继续使用新的迁移版本。

主键由 MyBatis-Plus 生成。超出 JavaScript 安全整数范围的 Long 在响应中输出字符串，日期时间默认使用 `yyyy-MM-dd HH:mm:ss`。

认证接口和异常处理器统一返回 `Result<T>`。`ResultResponseBodyAdvice` 按 `Result.code` 设置 HTTP 状态，响应体保持原样。成功通常为 200，参数错误为 400，未登录为 401，无权限为 403，冲突为 409，限流为 429，系统异常为 500。
