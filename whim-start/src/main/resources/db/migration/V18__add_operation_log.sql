CREATE TABLE sys_oper_log (
    id BIGINT NOT NULL,
    title VARCHAR(64) NOT NULL,
    log_type TINYINT NOT NULL,
    method_name VARCHAR(255) NULL,
    request_method VARCHAR(16) NULL,
    oper_name VARCHAR(32) NULL,
    oper_ip VARCHAR(45) NULL,
    oper_location VARCHAR(255) NULL,
    request_url VARCHAR(512) NULL,
    request_param TEXT NULL COMMENT '允许记录的业务字段',
    response_param TEXT NULL COMMENT '响应状态摘要',
    status TINYINT NOT NULL COMMENT '0成功，1失败',
    error_message VARCHAR(512) NULL,
    oper_time DATETIME NOT NULL,
    cost_time BIGINT NOT NULL COMMENT '耗时（毫秒）',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    KEY idx_oper_log_deleted_id (deleted, id),
    KEY idx_oper_log_time (deleted, oper_time, id),
    KEY idx_oper_log_name (oper_name, deleted, oper_time, id),
    CONSTRAINT chk_oper_log_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_oper_log_type CHECK (log_type BETWEEN 0 AND 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统操作日志';

INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, icon, built_in)
VALUES
    (3200, 'SysOperLog', '操作日志', 'system:operLog:page', 200, 2, '/system/operLog', 'system/operLog/index', 1, 'list', 1),
    (3201, NULL, '操作日志详情', 'system:operLog:detail', 3200, 3, NULL, NULL, 1, NULL, 1),
    (3202, NULL, '导出操作日志', 'system:operLog:export', 3200, 3, NULL, NULL, 2, NULL, 1),
    (3203, NULL, '删除操作日志', 'system:operLog:delete', 3200, 3, NULL, NULL, 3, NULL, 1),
    (3204, NULL, '清空操作日志', 'system:operLog:clean', 3200, 3, NULL, NULL, 4, NULL, 1);

INSERT INTO sys_dict_type (id, name, type, remark, create_by)
VALUES (2009, '操作类型', 'sys_oper_type', '系统操作日志使用的业务操作类型', 1);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES (20090, 'sys_oper_type', '新增', '0', 'success', 0, 1),
       (20091, 'sys_oper_type', '修改', '1', 'info', 1, 1),
       (20092, 'sys_oper_type', '删除', '2', 'error', 2, 1),
       (20093, 'sys_oper_type', '导出', '3', 'primary', 3, 1),
       (20094, 'sys_oper_type', '导入', '4', 'primary', 4, 1),
       (20095, 'sys_oper_type', '其他', '5', 'default', 5, 1),
       (20096, 'sys_oper_type', '清空', '6', 'warning', 6, 1);
