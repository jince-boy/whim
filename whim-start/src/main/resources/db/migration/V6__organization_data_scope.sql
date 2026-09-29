-- 成员的主部门归属属于租户关系；历史成员没有可信来源，保留 NULL，部门范围按空范围处理。
ALTER TABLE sys_user_tenant
    ADD COLUMN dept_id bigint(20) NULL DEFAULT NULL COMMENT '当前租户主部门ID' AFTER tenant_id,
    ADD KEY idx_user_tenant_tenant_dept (tenant_id, dept_id, status, deleted);

-- 岗位可绑定租户内部门；历史岗位保持未归属，不从用户岗位关系推断部门。
ALTER TABLE sys_post
    ADD COLUMN dept_id bigint(20) NULL DEFAULT NULL COMMENT '所属部门ID' AFTER tenant_id,
    ADD KEY idx_post_tenant_dept (tenant_id, dept_id, deleted);

-- 历史角色部门关联若租户不一致、角色不存在/已删除或部门不可用，保留记录但停止授权。
UPDATE sys_role_dept binding
    LEFT JOIN sys_role assigned_role
              ON assigned_role.id = binding.role_id
                  AND assigned_role.tenant_id = binding.tenant_id
                  AND assigned_role.role_type = 1
                  AND assigned_role.deleted = 0
    LEFT JOIN sys_dept department
              ON department.id = binding.dept_id
                  AND department.tenant_id = binding.tenant_id
                  AND department.deleted = 0
                  AND department.status = 0
SET binding.deleted = 1, binding.delete_time = CURRENT_TIMESTAMP
WHERE binding.deleted = 0
  AND (assigned_role.id IS NULL OR department.id IS NULL);

-- 新功能权限默认不授予已有角色或套餐，由管理员明确配置。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, sort, status, deleted, create_time)
SELECT maximum_id.max_id + ROW_NUMBER() OVER (ORDER BY catalog.perms),
       catalog.perms, catalog.menu_name, catalog.perms, 0, 3, 0, 0, 0, CURRENT_TIMESTAMP
FROM (
         SELECT 'system:dept:list' AS perms, '查看租户部门' AS menu_name
         UNION ALL SELECT 'system:dept:create', '创建租户部门'
         UNION ALL SELECT 'system:dept:update', '修改租户部门'
         UNION ALL SELECT 'system:dept:status', '修改租户部门状态'
         UNION ALL SELECT 'system:member:department', '分配租户成员部门'
         UNION ALL SELECT 'system:role:dataScope', '配置租户角色数据范围'
     ) catalog
         CROSS JOIN (SELECT COALESCE(MAX(id), 0) AS max_id FROM sys_permission) maximum_id
WHERE NOT EXISTS (SELECT 1 FROM sys_permission existing WHERE existing.perms = catalog.perms);
