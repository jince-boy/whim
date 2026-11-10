-- 已有文件均由文件管理入口上传，新增字段时沿用该来源；业务入口通过上传参数记录自己的途径。
ALTER TABLE sys_file
    ADD COLUMN upload_source VARCHAR(64) NOT NULL DEFAULT 'fileManagement' COMMENT '上传途径，关联sys_file_upload_source字典' AFTER platform,
    ADD INDEX idx_file_upload_source (upload_source, deleted, id);

INSERT INTO sys_dict_type (id, name, type, remark, create_by)
VALUES (2006, '文件上传途径', 'sys_file_upload_source', '文件所属业务上传入口，与存储平台和文件类型无关', 1);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES (20060, 'sys_file_upload_source', '文件管理', 'fileManagement', 'default', 0, 1);
