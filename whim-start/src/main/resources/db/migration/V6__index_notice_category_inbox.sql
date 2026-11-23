-- 按分类读取最近消息和历史分页时，直接使用可见条件和发布时间的索引顺序。
CREATE INDEX idx_notice_category_inbox
    ON sys_notice (deleted, status, notice_type, publish_time DESC, id DESC);
