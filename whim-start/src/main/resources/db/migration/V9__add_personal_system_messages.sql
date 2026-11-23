-- 收件人为空的存量通知和公告继续面向全员，系统消息必须有唯一收件人。
ALTER TABLE sys_notice
    MODIFY notice_type TINYINT NOT NULL COMMENT '1通知，2公告，3系统',
    ADD COLUMN recipient_user_id BIGINT NULL COMMENT '系统消息收件人' AFTER notice_type,
    ADD CONSTRAINT chk_notice_recipient CHECK (
        (notice_type IN (1, 2) AND recipient_user_id IS NULL) OR
        (notice_type = 3 AND recipient_user_id IS NOT NULL)
    ),
    ADD INDEX idx_notice_recipient_inbox (recipient_user_id, deleted, status, publish_time DESC, id DESC),
    ADD INDEX idx_notice_public_management (recipient_user_id, deleted, create_time DESC, id DESC);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort)
VALUES (20102, 'sys_notice_type', '系统', '3', 'info', 2);
