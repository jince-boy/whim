-- 菜单与权限统一管理，内置标记仅锁定授权语义，删除保护由核心管理节点规则决定。
ALTER TABLE sys_permission
    MODIFY COLUMN built_in TINYINT NOT NULL DEFAULT 0 COMMENT '内置定义不能改变授权语义，非核心节点允许删除';
