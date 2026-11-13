-- 头像与文件管理共用默认存储，单独标记来源，头像读取接口只允许访问该来源的图片。
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES (20061, 'sys_file_upload_source', '用户头像', 'userAvatar', 'default', 1, 1);
