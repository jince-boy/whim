# 部门与岗位后端

所有接口均需要系统账号登录，使用 `Authorization: Bearer <accessToken>`。业务数据按当前确切操作的授权角色范围过滤，部门“本人”范围使用部门负责人，岗位“本人”范围使用创建人 `create_by`，部门范围仍使用 `dept_id`。岗位只表达任职关系，不设置负责人，不授予角色或功能权限。创建人和创建时间由 MyBatis-Plus 自动填充，修改人和修改时间在更新时自动填充；业务方法不手动设置或回填审计字段，业务请求不能指定审计字段。

## 接口

| 方法 | 路径 | 用途 | 权限码 |
| --- | --- | --- | --- |
| GET | `/system/dept/list` | 部门树与上级选择 | `system:dept:list` |
| GET | `/system/dept/detail` | 修改表单详情，ID在查询参数中 | `system:dept:update` |
| GET | `/system/dept/leaderOptions` | 本部门负责人候选用户分页，部门ID在查询参数中 | `system:dept:list` |
| POST | `/system/dept/add` | 新增部门 | `system:dept:create` |
| PUT | `/system/dept/update` | 完整修改部门，ID在Body中 | `system:dept:update` |
| PUT | `/system/dept/status` | 修改部门状态，ID在Body中 | `system:dept:status` |
| DELETE | `/system/dept/delete` | 删除部门，ID在查询参数中 | `system:dept:delete` |
| GET | `/system/post/page` | 岗位分页 | `system:post:list` |
| GET | `/system/post/detail` | 岗位详情，ID在查询参数中 | `system:post:detail` |
| POST | `/system/post/add` | 新增岗位，仅返回操作结果 | `system:post:create` |
| PUT | `/system/post/update` | 完整修改岗位，ID在Body中 | `system:post:update` |
| PUT | `/system/post/updateStatus` | 修改岗位状态，ID在Body中 | `system:post:status` |
| DELETE | `/system/post/delete` | 删除空岗位，ID在查询参数中 | `system:post:delete` |
| GET | `/system/post/memberPage` | 成员或候选用户分页，postId与筛选条件统一放在查询参数中 | `system:post:member:list` |
| POST | `/system/post/addMembers` | 增量添加成员，postId和userIds统一放在Body中 | `system:post:member:assign` |
| DELETE | `/system/post/deleteMembers` | 解除指定成员关联，postId和userIds统一放在Body中 | `system:post:member:assign` |

沿用初始化脚本中的权限码，部门详情供修改表单回填，使用部门修改权限。部门接口路径采用与字典模块一致的 `/list`、`/detail`、`/add`、`/update`、`/delete` 命名；详情和删除通过查询参数传递ID，例如 `GET /system/dept/detail?id=1`、`DELETE /system/dept/delete?id=1`。分页使用 `pageNum`（默认1）和 `pageSize`（默认10，最大100）。响应使用 `Result<T>`；分页内容位于 `data.data`，并包含 `currentPage/pages/size/total`。Long 在 JavaScript 安全整数范围内输出数字，超出范围输出十进制字符串；客户端应保留ID精度。

部门树通过 GET 查询参数绑定 `SysDeptQueryDTO`，支持可选的 `deptName` 名称模糊筛选（最多64字符，空白时不筛选）和 `status` 状态精确筛选（0正常、1停用，省略时不筛选）。两个条件同时提供时取交集；不传条件返回完整的可见部门树，无匹配结果返回空数组，不分页。例如 `GET /system/dept/list?deptName=研发&status=0` 查询名称包含“研发”的正常部门。只将匹配且有权查看的部门组装成树；上级未匹配或不可见时，当前部门作为返回树的根节点，保留原始 `parentId`，不补查祖先。返回节点按排序值和ID升序排列，停用部门仍能查询和维护。上级选择框在前端排除当前部门及其下级分支，不接收 `excludeDeptId` 查询参数。

部门列表及详情增加 `leaderName/leaderUsername`，仅在负责人仍是本部门正常用户时返回姓名和用户名，否则为null。负责人候选接口属于部门查询能力，功能权限和数据范围统一使用 `system:dept:list`，无需部门修改权限；先确认目标部门在查询范围内，再查询主部门为目标部门、正常且未删除的用户，不包含下级部门用户。部门不存在或超出查询范围时返回500及“部门不存在或无权查看”。必填的 `deptId`、可选的 `keyword`（按姓名或用户名模糊搜索，最多64字符）及标准 `pageNum/pageSize` 分页统一放在 `SysDeptLeaderQueryDTO` 中，通过 GET 查询参数提交，例如 `/system/dept/leaderOptions?deptId=1&pageNum=1&pageSize=20`；缺少部门ID返回400。控制器和服务均只接收该DTO，不再单独传部门ID。选项仅包含 `id/username/name`，不返回密码或联系方式。保存负责人仍由部门修改接口校验 `system:dept:update` 及其数据范围，并重新校验用户的部门归属和状态。

岗位分页支持 `postName/postCode` 模糊筛选（最多64字符）、`deptId/status` 精确筛选，按 `sort/id` 升序返回。`organizationWide=true` 只查询 `deptId IS NULL` 的全组织岗位，不能同时指定 `deptId`，冲突返回400；false或省略时不限制岗位范围。全组织岗位仍受操作级数据权限限制，不意味着所有用户都能查看或修改。详情和删除使用查询参数，例如 `GET /system/post/detail?id=1`、`DELETE /system/post/delete?id=1`。

成员分页使用 `GET /system/post/memberPage?postId=1&assigned=true&pageNum=1&pageSize=10`，支持 `username/name` 模糊筛选（分别最多32、24字符）、`deptId/status` 精确筛选；`assigned=true`（默认且不能为null）查询已分配的未删除用户，`assigned=false` 查询未分配的正常用户并忽略status筛选。岗位和用户都必须在该成员操作的授权范围内。成员返回 `userId/username/name/deptId/status`，未分配主部门时deptId为null，不返回密码和联系方式；成员关联条件和数据权限在数据库分页前生效。

## JSON 请求

新增部门使用以下 Body，`parentId/deptName/deptCode` 必填。`parentId=0` 表示根部门；`sort` 省略时为0。部门编码最多64字符，名称和编码保存时去除首尾空格，未删除部门之间编码唯一。新增部门时 `leaderUserId` 必须省略或为null；先创建部门，再通过用户管理将用户归属到该部门，最后编辑部门选择负责人。修改时负责人必须是主部门为当前部门的正常用户，可省略或传null清空。电话、邮箱和备注省略或传null均清空。祖先路径、审计字段和删除标识不接受业务请求赋值。

```json
{
  "parentId": 1,
  "deptName": "研发部",
  "deptCode": "research",
  "leaderUserId": null,
  "phone": null,
  "email": null,
  "sort": 0,
  "status": 0,
  "remark": null
}
```

完整修改部门使用 `PUT /system/dept/update`，在同一个 Body 中提交必填的 `id` 和全部可修改字段，不再使用路径参数传递ID。修改请求示例：

```json
{
  "id": 2,
  "parentId": 1,
  "deptName": "研发部",
  "deptCode": "research",
  "leaderUserId": null,
  "phone": null,
  "email": null,
  "sort": 0,
  "status": 0,
  "remark": null
}
```

新增岗位使用以下 Body，`postName/postCode` 必填。`deptId=null` 表示全组织岗位；`sort` 省略时为0。部门和备注省略或传null均清空，名称及编码去除首尾空格。岗位请求及响应均不包含负责人字段。

```json
{
  "deptId": 1,
  "postName": "研发工程师",
  "postCode": "developer",
  "sort": 0,
  "status": 0,
  "remark": null
}
```

完整修改岗位使用 `PUT /system/post/update`，在同一个 Body 中提交必填的 `id` 和全部可修改字段，不再使用路径参数传递ID。修改请求示例：

```json
{
  "id": 1,
  "deptId": 1,
  "postName": "研发工程师",
  "postCode": "developer",
  "sort": 0,
  "status": 0,
  "remark": null
}
```

新增接口不接收主键；完整修改和状态修改的 `id` 必填且不能为null，缺失时返回400。ID沿用大于0的项目约定，Long可使用数字或十进制字符串，前端保留其原始精度。ID只用于定位目标记录，不作为可修改的业务字段。

新增部门、岗位的 `sort/status` 省略时默认为0，显式null不通过校验；完整修改时两个字段均必填。修改权限包含表单内的状态修改。部门状态独立维护，正常岗位须通过所属部门及祖先有效性检查。岗位列表快捷状态开关调用 `PUT /system/post/updateStatus`，使用 `system:post:status` 权限，Body为 `{"id": 1, "status": 0}`，两个字段均必填，0正常、1停用；状态修改保留其他字段，相同状态不重复写入或失效会话。部门页面当前仍调用尚未实现的 `/system/dept/status`，不属于本次岗位接口。

成员添加和解除关联的Body统一为 `{"postId": 1, "userIds": [2, 3]}`，postId必填，用户集合必填、非空、最多500个不同ID且不能包含null。以上ID均须替换为实际业务ID。岗位和全部用户须在成员配置操作的授权范围内，任一用户不符合条件时整批拒绝；添加只接受正常用户，已有关联自动忽略，全部已分配时直接成功。解除允许用户或岗位已停用，无匹配关联时直接成功，不覆盖或删除其他成员。只对实际发生关联变更的用户在提交后失效会话。

这些接口兼容业务JSON，也自动识别公共请求层生成的JWE密文包装，协议见 [请求加密](request-encryption.md)。Apifox 中直接维护 Body 字段、约束和示例，不创建调试用例；文档示例不代表运行页已填入可用密文。

## 业务边界与事务

- 未删除的同级部门名称唯一，未删除的部门编码和岗位编码各自在全组织内唯一。逻辑删除后可以复用，依靠联合唯一索引处理并发冲突。
- 部门负责人在用户归属部门后，从本部门正常用户中选择，后端同时校验部门归属及用户状态。修改时校验目标部门及新负责人归属的数据权限；更换为非根上级时，同时校验新上级的修改权限。`parentId=0` 表示移动到根节点，不额外要求全部数据范围。禁止以自身或下级作为上级；移动时批量更新全部下级路径，写入仍受数据权限过滤，未能完整更新时回滚整个修改。
- 部门状态独立维护，允许在停用的祖先下设置为启用，不自动启用祖先，也不覆盖下级状态。有效登录授权仍要求部门自身及整条祖先链启用；停用祖先后，其下级分支退出有效范围，重新启用后按各部门原状态恢复。
- 部门存在下级、未删除用户、未删除岗位或角色自定义数据范围引用时禁止删除。停用的子部门、用户和岗位同样阻止删除。
- 正常岗位必须属于有效部门或为全组织岗位。停用岗位或所属部门无效时不能添加成员。解除成员关联允许岗位和用户已停用；岗位有任何成员关联时禁止删除。
- 部门、岗位及成员写操作使用 `@Lock4j(name = "system:organization")`，复用现有 Redisson 分布式锁，避免部门移动、删除、岗位归属变更和成员配置之间发生竞争。锁由切面在进入事务前获取，在事务结束后释放；Redisson 自动续期，不依赖 `sys_config` 配置记录。空岗位删除后，岗位编码可以重新使用。
- 部门名称或负责人变化时，仅使该部门主部门用户的会话失效；部门移动或启停时，查询受影响的主部门用户、岗位成员、角色自定义范围持有者，并补充相关祖先部门的下级范围持有者。祖先部门用户通过 MyBatis-Plus 按用户、用户角色关系、有效角色进行批量查询，不逐个用户查库。部门电话、邮箱、编码、排序和备注变更不失效会话。会话失效在事务提交后执行，重新登录时构建新的授权快照；岗位名称、编码、所属部门或状态变化使全部成员会话失效，单独修改岗位排序或备注不失效会话。岗位成员变更仅影响本次实际新增或解除的成员。

`V5__initialize_organization_management.sql` 为历史迁移，包含部门负责人查询索引和旧的组织锁配置记录。新增的 `V8__remove_configuration_lock_entries.sql` 清理 `system.organization.lock` 与 `system.menu.lock` 两条技术锁记录；历史版本化迁移保持原样。菜单维护同样改用独立的 `@Lock4j(name = "system:menu")`。`sys_config` 只保存业务配置。

`V7__remove_post_owner.sql` 移除岗位负责人外键、索引和字段，并为创建人数据范围增加索引。已有岗位继续保留真实的 `create_by` 审计信息，不将旧负责人转换为创建人。前端岗位表单通过部门树选择所属部门，所属部门与显示排序各占一行。

`V6__add_department_code.sql` 新增部门编码字段，为已有记录按 `DEPT_主键ID` 回填可编辑的初始编码，再建立 `(dept_code, deleted)` 联合唯一索引。列表和按ID读取的编辑数据均包含 `deptCode`。部门页面仅展示名称、编码、排序、状态和创建时间等业务列；取消单独的查看入口，读取详情接口仍供修改表单回填使用。

参数或JSON校验错误返回400，未认证返回401，功能或数据权限拒绝返回403，数据库唯一约束冲突返回409；沿用当前全局异常处理，业务规则拒绝通过 `ServiceException` 返回500。密文解密限流返回429。HTTP状态与 `Result.code` 保持一致。

修改接口及服务只接收一个包含ID和可修改字段的DTO，保存流程将该DTO转换成单个实体后执行 `updateById`，不再单独传ID或手动 `setId`。允许清空的字段通过实体更新策略维护。读取旧部门用于比较变化和替换路径，不复制第二个更新实体；仅移动部门时更新下级路径，以实际更新数量保证完整性，不再提前重复统计下级权限。
