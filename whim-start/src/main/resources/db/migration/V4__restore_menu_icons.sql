-- V2 保持已执行的原文；展示调整通过新迁移追加，避免破坏 Flyway 校验。
-- 仅修正 V2 写入的默认图标，保留已经自定义的其他图标。
UPDATE sys_permission SET icon = 'yonghu'
WHERE id = 1500 AND built_in = 1 AND icon = 'menu' AND deleted = 0;

UPDATE sys_permission SET icon = NULL
WHERE id = 100 AND built_in = 1 AND icon = 'setting' AND deleted = 0;
