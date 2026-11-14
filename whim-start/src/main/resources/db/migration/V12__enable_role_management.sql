-- 启用已有角色菜单，保留各操作独立的权限码与授权关系。
UPDATE sys_permission SET visible = 0 WHERE id = 1100 AND deleted = 0;

INSERT INTO sys_permission
    (id, menu_name, perms, parent_id, menu_type, sort, built_in, create_by)
VALUES (1109, '角色详情', 'system:role:detail', 1100, 3, 9, 1, 1);

INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
VALUES (181109, 1, 1109, 1);

-- 所有角色授予的操作共用一个数据范围，前端统一使用真实字典。
INSERT INTO sys_dict_type (id, name, type, remark, create_by)
VALUES (2007, '角色数据范围', 'sys_role_data_scope', '每个角色的全部操作共用同一个数据范围', 1);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES
    (20070, 'sys_role_data_scope', '全部数据', '1', 'primary', 0, 1),
    (20071, 'sys_role_data_scope', '自定义部门', '2', 'info', 1, 1),
    (20072, 'sys_role_data_scope', '本部门', '3', 'default', 2, 1),
    (20073, 'sys_role_data_scope', '本部门及下级部门', '4', 'default', 3, 1),
    (20074, 'sys_role_data_scope', '仅本人', '5', 'default', 4, 1);
