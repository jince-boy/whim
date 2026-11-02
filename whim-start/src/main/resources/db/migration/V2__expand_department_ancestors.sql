-- 部门祖先路径改用文本类型，移除原有的500字符限制。
-- 兼容MySQL 8.0.12，TEXT不设置默认值；新增部门时由服务明确写入路径。
ALTER TABLE sys_dept
    MODIFY COLUMN ancestors TEXT NOT NULL COMMENT '祖级部门ID路径，逗号分隔';
