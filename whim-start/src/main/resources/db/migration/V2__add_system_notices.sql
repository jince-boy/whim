CREATE TABLE sys_notice (
    id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    notice_type TINYINT NOT NULL COMMENT '1通知，2公告',
    content TEXT NOT NULL COMMENT '纯文本正文',
    status TINYINT NOT NULL COMMENT '0正常，1关闭',
    publish_time DATETIME(3) NULL COMMENT '最近一次发布时间',
    version INT NOT NULL DEFAULT 0 COMMENT '编辑版本',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_notice_inbox (deleted, status, publish_time DESC, id DESC),
    KEY idx_notice_management (deleted, create_time DESC, id DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知公告';

CREATE TABLE sys_notice_read (
    id BIGINT NOT NULL,
    notice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次阅读时间',
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_read_user (user_id, notice_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户公告阅读记录';

INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (2100, 100, 'SysNotice', '通知公告', 'system:notice:page', 2, '/system/notice', 'system/notice/index', 0, 0, 90, 'bell', 1);

INSERT INTO sys_permission (id, parent_id, menu_name, perms, menu_type, sort, built_in) VALUES
    (2101, 2100, '公告详情', 'system:notice:detail', 3, 1, 1),
    (2102, 2100, '新增公告', 'system:notice:add', 3, 2, 1),
    (2103, 2100, '修改公告', 'system:notice:update', 3, 3, 1),
    (2104, 2100, '删除公告', 'system:notice:delete', 3, 4, 1);

INSERT INTO sys_dict_type (id, name, type) VALUES
    (2010, '公告类型', 'sys_notice_type'), (2011, '公告状态', 'sys_notice_status');
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort) VALUES
    (20100, 'sys_notice_type', '通知', '1', 'info', 0),
    (20101, 'sys_notice_type', '公告', '2', 'primary', 1),
    (20110, 'sys_notice_status', '正常', '0', 'success', 0),
    (20111, 'sys_notice_status', '关闭', '1', 'default', 1);
