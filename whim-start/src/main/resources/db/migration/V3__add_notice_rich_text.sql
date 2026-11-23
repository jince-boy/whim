ALTER TABLE sys_notice
    ADD COLUMN content_html MEDIUMTEXT NULL COMMENT '清洗后的公告富文本，纯文本公告为空' AFTER content;
