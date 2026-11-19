CREATE TABLE sys_login_log (
    id BIGINT NOT NULL,
    username VARCHAR(32) NOT NULL COMMENT '提交登录的用户名',
    ip_address VARCHAR(45) NULL,
    login_location VARCHAR(255) NULL,
    browser VARCHAR(128) NULL,
    os VARCHAR(128) NULL,
    device_type VARCHAR(64) NULL,
    status TINYINT NOT NULL COMMENT '0成功，1失败',
    remark VARCHAR(255) NOT NULL COMMENT '认证结果说明',
    login_time DATETIME NOT NULL COMMENT '本次认证结果确定的时间',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    KEY idx_login_log_deleted_id (deleted, id),
    KEY idx_login_log_time (deleted, login_time, id),
    KEY idx_login_log_username (username, deleted, login_time, id),
    KEY idx_login_log_ip (ip_address, deleted, login_time, id),
    CONSTRAINT chk_login_log_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统登录日志';

-- 日志属于系统级审计数据，普通角色需要分别获得相应操作的授权。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, icon, built_in)
VALUES
    (200, 'LogManagement', '日志管理', NULL, 0, 1, '/logs', NULL, 20, 'scroll-text', 1),
    (3100, 'SysLoginLog', '登录日志', 'system:loginLog:page', 200, 2, '/system/loginLog', 'system/loginLog/index', 0, 'log-in', 1),
    (3101, NULL, '导出登录日志', 'system:loginLog:export', 3100, 3, NULL, NULL, 1, NULL, 1),
    (3102, NULL, '删除登录日志', 'system:loginLog:delete', 3100, 3, NULL, NULL, 2, NULL, 1),
    (3103, NULL, '清空登录日志', 'system:loginLog:clean', 3100, 3, NULL, NULL, 3, NULL, 1);

INSERT INTO sys_dict_type (id, name, type, remark, create_by)
VALUES (2008, '执行结果', 'sys_result_status', '0成功，1失败', 1);
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES (20080, 'sys_result_status', '成功', '0', 'success', 0, 1),
       (20081, 'sys_result_status', '失败', '1', 'error', 1, 1);
