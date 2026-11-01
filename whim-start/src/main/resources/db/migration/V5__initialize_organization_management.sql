-- 部门层级、岗位归属和岗位成员配置共用事务锁，避免并发维护破坏引用与祖先路径。
INSERT INTO sys_config (id, config_key, config_value, remark, create_by)
VALUES (3, 'system.organization.lock', '1', '部门及岗位并发维护锁', NULL);

-- 支持部门负责人“本人”范围的查询与维护，岗位负责人已有对应索引。
CREATE INDEX idx_dept_leader ON sys_dept (leader_user_id, deleted);
