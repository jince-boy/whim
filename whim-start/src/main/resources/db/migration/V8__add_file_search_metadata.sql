-- 文件名与后缀作为独立的可查询元数据，避免把对象路径中的目录当成文件名称。
ALTER TABLE sys_file
    ADD COLUMN file_name VARCHAR(512) NULL COMMENT '存储文件名称，不含目录' AFTER platform,
    ADD COLUMN file_suffix VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原文件后缀，不含点号' AFTER original_name;

-- 仅补齐技术元数据，保留历史文件及原有审计信息。
UPDATE sys_file
SET file_name = SUBSTRING_INDEX(object_key, '/', -1),
    file_suffix = IF(LOCATE('.', original_name) > 0, SUBSTRING_INDEX(original_name, '.', -1), '');

ALTER TABLE sys_file
    MODIFY COLUMN file_name VARCHAR(512) NOT NULL COMMENT '存储文件名称，不含目录',
    ADD INDEX idx_file_suffix (file_suffix, deleted, id),
    ADD INDEX idx_file_create_time (deleted, create_time, id);
