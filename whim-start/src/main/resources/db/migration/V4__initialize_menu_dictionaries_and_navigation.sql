-- 初始化菜单字典。数值编码与后端字段约定一致，标签、排序和回显样式由字典管理维护。
-- 保留已有同编码字典及选项；历史迁移不变，空库和已有数据库均通过本版本补齐默认数据。
INSERT INTO sys_dict_type (id, name, type, remark, create_by)
SELECT seed.id, seed.name, seed.type, seed.remark, 1
FROM (
    SELECT 2000 AS id, '菜单类型' AS name, 'sys_menu_type' AS type,
           '对应菜单类型编码：1目录，2菜单，3按钮，4外链' AS remark
    UNION ALL SELECT 2001, '菜单状态', 'sys_menu_status', '对应菜单启停状态：0启用，1停用'
    UNION ALL SELECT 2002, '菜单显示状态', 'sys_menu_visible', '对应菜单可见性：0显示，1隐藏'
    UNION ALL SELECT 2003, '菜单缓存状态', 'sys_menu_cache', '对应页面缓存配置：0不缓存，1缓存'
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type AS existing
    WHERE existing.type = seed.type AND existing.deleted = 0
);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
SELECT seed.id, seed.type, seed.label, seed.value, seed.list_class, seed.sort, 1
FROM (
    SELECT 20000 AS id, 'sys_menu_type' AS type, '目录' AS label, '1' AS value,
           'info' AS list_class, 0 AS sort
    UNION ALL SELECT 20001, 'sys_menu_type', '菜单', '2', 'success', 1
    UNION ALL SELECT 20002, 'sys_menu_type', '按钮', '3', 'warning', 2
    UNION ALL SELECT 20003, 'sys_menu_type', '外链', '4', 'primary', 3
    UNION ALL SELECT 20010, 'sys_menu_status', '启用', '0', 'success', 0
    UNION ALL SELECT 20011, 'sys_menu_status', '停用', '1', 'error', 1
    UNION ALL SELECT 20020, 'sys_menu_visible', '显示', '0', 'success', 0
    UNION ALL SELECT 20021, 'sys_menu_visible', '隐藏', '1', 'warning', 1
    UNION ALL SELECT 20030, 'sys_menu_cache', '不缓存', '0', 'default', 0
    UNION ALL SELECT 20031, 'sys_menu_cache', '缓存', '1', 'success', 1
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data AS existing
    WHERE existing.type = seed.type AND existing.value = seed.value AND existing.deleted = 0
);

-- 页面统一命名为字典管理，接口路径、权限码及已有授权关系保持稳定。
UPDATE sys_permission
SET menu_name = CASE id
        WHEN 1600 THEN '字典管理'
        WHEN 1601 THEN '字典详情'
        WHEN 1602 THEN '新增字典'
        WHEN 1603 THEN '修改字典'
        WHEN 1604 THEN '删除字典'
        WHEN 1605 THEN '导出字典'
    END,
    update_time = CURRENT_TIMESTAMP(3)
WHERE id IN (1600, 1601, 1602, 1603, 1604, 1605)
  AND built_in = 1 AND deleted = 0;

-- 为默认目录和页面补齐 Lucide 图标，同时纠正旧的首页、菜单管理默认图标。
-- 已经自行选择的其他图标保留；按钮权限不作为导航菜单，无需配置导航图标。
UPDATE sys_permission
SET icon = CASE id
        WHEN 90 THEN 'house'
        WHEN 100 THEN 'settings'
        WHEN 1000 THEN 'users'
        WHEN 1100 THEN 'shield-check'
        WHEN 1200 THEN 'network'
        WHEN 1300 THEN 'briefcase-business'
        WHEN 1500 THEN 'menu'
        WHEN 1600 THEN 'book-open'
        WHEN 1700 THEN 'database'
    END,
    update_time = CURRENT_TIMESTAMP(3)
WHERE id IN (90, 100, 1000, 1100, 1200, 1300, 1500, 1600, 1700)
  AND built_in = 1 AND deleted = 0
  AND (icon IS NULL OR TRIM(icon) = ''
       OR (id = 90 AND icon = 'smile')
       OR (id = 1500 AND icon IN ('user', 'yonghu')));
