-- 部门编码为真实业务字段，现有记录按主键生成可编辑且不冲突的初始编码。
ALTER TABLE sys_dept
    ADD COLUMN dept_code VARCHAR(64) NULL COMMENT '部门编码' AFTER dept_name;

UPDATE sys_dept
SET dept_code = CONCAT('DEPT_', id)
WHERE dept_code IS NULL;

-- 保留逻辑删除后复用编码的能力，未删除部门之间编码唯一。
ALTER TABLE sys_dept
    MODIFY COLUMN dept_code VARCHAR(64) NOT NULL COMMENT '部门编码',
    ADD UNIQUE KEY uk_dept_code (dept_code, deleted) COMMENT '未删除的部门编码唯一';
