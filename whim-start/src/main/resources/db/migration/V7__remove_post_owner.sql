-- 岗位仅表达任职关系，不设置负责人；本人数据范围使用不可由业务请求修改的创建人。
ALTER TABLE sys_post
    DROP FOREIGN KEY fk_post_owner,
    DROP INDEX idx_post_owner,
    DROP COLUMN owner_user_id,
    ADD KEY idx_post_creator (create_by, deleted);
