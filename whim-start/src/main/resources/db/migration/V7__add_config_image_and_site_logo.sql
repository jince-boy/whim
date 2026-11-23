ALTER TABLE sys_config
    MODIFY value_type ENUM('TEXT', 'RADIO', 'JSON', 'IMAGE') NOT NULL DEFAULT 'TEXT' COMMENT '值类型';

INSERT INTO sys_config (id, config_name, config_key, value_type, remark, built_in)
VALUES (5, '系统 Logo', 'system.site.logo', 'IMAGE', '登录页与导航使用的标志，清空后恢复默认 Logo', 1);

INSERT INTO sys_dict_type (id, name, type)
VALUES (2012, '配置值类型', 'sys_config_value_type');

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort)
VALUES (20120, 'sys_config_value_type', '文本', 'TEXT', 'default', 0),
       (20121, 'sys_config_value_type', '单选', 'RADIO', 'default', 1),
       (20122, 'sys_config_value_type', 'JSON', 'JSON', 'default', 2),
       (20123, 'sys_config_value_type', '图片', 'IMAGE', 'default', 3),
       (20063, 'sys_file_upload_source', '配置图片', 'configImage', 'info', 3);
