ALTER TABLE sys_notice_read
    ADD COLUMN read_version INT NOT NULL DEFAULT 0 COMMENT '用户已读的最新公告版本' AFTER user_id;

-- 旧记录未保存读者看到的版本，以升级时的版本承接原有已读状态。
UPDATE sys_notice_read r
INNER JOIN sys_notice n ON n.id = r.notice_id
SET r.read_version = n.version
WHERE r.deleted = 0 AND n.deleted = 0 AND n.version > r.read_version;
