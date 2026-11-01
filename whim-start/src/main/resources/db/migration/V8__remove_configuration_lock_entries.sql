-- sys_config 仅保存业务配置，移除历史迁移中用于加锁的占位记录。
-- 组织与菜单维护已改用 Lock4j + Redisson，不再依赖数据库配置记录。
DELETE FROM sys_config
WHERE config_key IN ('system.organization.lock', 'system.menu.lock');
