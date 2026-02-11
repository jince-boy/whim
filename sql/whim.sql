/*
 Navicat Premium Dump SQL

 Source Server         : phpstudy
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : whim

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 11/02/2026 23:06:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(20) NOT NULL COMMENT '部门ID',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父部门ID(0表示根部门)',
  `ancestors` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '祖级列表(逗号分隔，如:0,1,2)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编码',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  `leader_id` bigint(20) NULL DEFAULT NULL COMMENT '部门负责人ID',
  `leader_name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门负责人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-正常 1-停用)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE COMMENT '部门编码唯一索引',
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '父部门ID索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 0, '0', '中国公司', '1', 1, NULL, NULL, NULL, NULL, 0, '中国公司', 1, '2025-07-09 22:55:43', NULL, '2025-07-09 22:55:43', 0);
INSERT INTO `sys_dept` VALUES (2, 1, '0,1', '河北公司', '2', 1, NULL, NULL, NULL, NULL, 0, '河北公司', 1, '2025-07-09 22:54:24', NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (3, 1, '0,1', '山西公司', '3', 2, NULL, NULL, NULL, NULL, 0, '山西公司', 1, '2025-07-09 22:55:23', NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (4, 2, '0,1,2', '研发部门', '4', 1, NULL, NULL, NULL, NULL, 0, '研发部门', 1, '2025-07-09 22:57:58', NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (5, 2, '0,1,2', '财务部门', '5', 2, NULL, NULL, NULL, NULL, 0, '财务部门', 1, '2025-07-09 22:58:29', NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (6, 3, '0,1,3', '市场部门', '6', 1, NULL, NULL, NULL, NULL, 0, '市场部门', 1, '2025-07-09 22:59:16', NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (7, 3, '0,1,3', '运维部门', '7', 2, NULL, NULL, NULL, NULL, 0, '运维部门', 1, '2025-07-09 22:59:50', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint(20) NOT NULL COMMENT '字典数据ID',
  `dict_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型(关联sys_dict_type.type)',
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典标签(显示文本)',
  `value` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典键值(存储值)',
  `list_class` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回显样式类名(如: primary)',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type` ASC) USING BTREE COMMENT '字典类型索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1958015172775395330, 'sys_result_status', '成功', '0', 'success', 0, '成功', '1', '2025-09-15 22:38:12', '1', '2025-09-15 22:38:12');
INSERT INTO `sys_dict_data` VALUES (1958015429949145090, 'sys_result_status', '失败', '1', 'error', 1, '失败', '1', '2025-09-15 22:38:12', '1', '2025-09-15 22:38:12');
INSERT INTO `sys_dict_data` VALUES (1958017291884576769, 'sys_oper_type', '新增', '0', 'primary', 0, '新增', '1', '2025-08-20 12:04:35', '1', '2025-08-20 12:04:35');
INSERT INTO `sys_dict_data` VALUES (1958017382099861505, 'sys_oper_type', '修改', '1', 'info', 1, '修改', '1', '2025-08-20 12:04:57', '1', '2025-08-20 12:04:57');
INSERT INTO `sys_dict_data` VALUES (1958017479806173186, 'sys_oper_type', '删除', '2', 'error', 2, '删除', '1', '2025-08-20 12:05:20', '1', '2025-08-20 12:05:20');
INSERT INTO `sys_dict_data` VALUES (1958017542217416706, 'sys_oper_type', '导出', '3', 'info', 3, '导出', '1', '2025-08-20 12:05:35', '1', '2025-08-20 12:05:35');
INSERT INTO `sys_dict_data` VALUES (1958017605383634946, 'sys_oper_type', '导入', '4', 'info', 4, '导入', '1', '2025-08-20 12:05:50', '1', '2025-08-20 12:05:50');
INSERT INTO `sys_dict_data` VALUES (1958017684475625473, 'sys_oper_type', '其他', '5', 'info', 5, '其他', '1', '2025-08-20 12:06:09', '1', '2025-08-20 12:06:09');
INSERT INTO `sys_dict_data` VALUES (1958017746136088577, 'sys_oper_type', '清空', '6', 'error', 6, '清空', '1', '2025-08-20 16:46:00', '1', '2025-08-20 16:46:01');
INSERT INTO `sys_dict_data` VALUES (1965418209726885889, 'sys_menu_type', '目录', '1', 'primary', 1, '目录', '1', '2025-09-09 22:13:12', '1', '2025-09-09 22:13:12');
INSERT INTO `sys_dict_data` VALUES (1965418352660377602, 'sys_menu_type', '菜单', '2', 'info', 2, '菜单', '1', '2025-09-09 22:13:56', '1', '2025-09-09 22:13:56');
INSERT INTO `sys_dict_data` VALUES (1965418478762127361, 'sys_menu_type', '按钮', '3', 'default', 3, '按钮', '1', '2025-09-09 22:14:16', '1', '2025-09-09 22:14:16');
INSERT INTO `sys_dict_data` VALUES (1965418526220677121, 'sys_menu_type', '外链', '4', 'default', 4, '外链', '1', '2025-09-09 22:14:27', '1', '2025-09-09 22:14:27');
INSERT INTO `sys_dict_data` VALUES (1965441018746314753, 'sys_show_status', '显示', '0', 'success', 1, '显示', '1', '2025-09-09 23:43:50', '1', '2025-09-09 23:43:50');
INSERT INTO `sys_dict_data` VALUES (1965441084655607809, 'sys_show_status', '隐藏', '1', 'error', 2, '隐藏', '1', '2025-09-09 23:44:05', '1', '2025-09-09 23:44:05');
INSERT INTO `sys_dict_data` VALUES (1967599543778000897, 'sys_run_status', '正常', '0', 'success', 1, '正常', '1', '2025-09-15 22:48:25', '1', '2025-09-15 22:48:26');
INSERT INTO `sys_dict_data` VALUES (1967599597670612994, 'sys_run_status', '停止', '1', 'error', 2, '停止', '1', '2025-09-15 22:41:15', '1', '2025-09-15 22:41:15');
INSERT INTO `sys_dict_data` VALUES (1968323243934404609, 'sys_menu_keepAlive_status', '缓存', '1', 'success', 1, '缓存', '1', '2025-09-17 22:36:46', '1', '2025-09-17 22:36:46');
INSERT INTO `sys_dict_data` VALUES (1968323309218746369, 'sys_menu_keepAlive_status', '不缓存', '0', 'info', 2, '不缓存', '1', '2025-09-17 22:37:08', '1', '2025-09-17 22:37:08');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(20) NOT NULL COMMENT '字典ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型(唯一标识)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type`(`type` ASC) USING BTREE COMMENT '字典类型唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1958014435433598977, '结果状态', 'sys_result_status', '结果状态', 1, '2025-09-15 22:38:12', 1, '2025-09-15 22:38:12');
INSERT INTO `sys_dict_type` VALUES (1958016228653678594, '操作类型', 'sys_oper_type', '操作类型', 1, '2025-08-20 12:00:22', 1, '2025-08-20 12:00:22');
INSERT INTO `sys_dict_type` VALUES (1965418075483992065, '菜单类型', 'sys_menu_type', '菜单类型', 1, '2025-09-09 22:12:40', 1, '2025-09-09 22:12:40');
INSERT INTO `sys_dict_type` VALUES (1965440928774299649, '显示状态', 'sys_show_status', '显示状态', 1, '2025-09-09 23:43:28', 1, '2025-09-09 23:43:28');
INSERT INTO `sys_dict_type` VALUES (1967599466741219330, '运行状态', 'sys_run_status', '运行状态', 1, '2025-09-15 22:40:44', 1, '2025-09-15 22:40:44');
INSERT INTO `sys_dict_type` VALUES (1968323084001398786, '系统菜单缓存状态', 'sys_menu_keepAlive_status', '系统菜单缓存状态', 1, '2025-09-17 22:36:08', 1, '2025-09-17 22:36:08');

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `username` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `browser` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `device_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '设备类型',
  `login_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '登录地点',
  `ip_address` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT 'ip地址',
  `status` tinyint(1) NOT NULL COMMENT '登录状态',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `login_time` datetime NULL DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '系统登录日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------
INSERT INTO `sys_login_log` VALUES (1962416361159925762, 'admin', 'Chrome 13', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-01 15:24:55');
INSERT INTO `sys_login_log` VALUES (1963231380135141377, 'admin', 'Chrome 13', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-03 21:23:31');
INSERT INTO `sys_login_log` VALUES (1965322599870930945, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-09 15:53:16');
INSERT INTO `sys_login_log` VALUES (1965323512043970561, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-09 15:56:54');
INSERT INTO `sys_login_log` VALUES (1966500727549046785, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-12 21:54:44');
INSERT INTO `sys_login_log` VALUES (1968321042818191361, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-17 22:28:01');
INSERT INTO `sys_login_log` VALUES (1971799127412695041, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 1, '用户名或密码错误', '2025-09-27 12:48:41');
INSERT INTO `sys_login_log` VALUES (1971799169087299585, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-09-27 12:48:51');
INSERT INTO `sys_login_log` VALUES (1979923916555767810, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 1, '用户名或密码错误', '2025-10-19 22:53:42');
INSERT INTO `sys_login_log` VALUES (1979923949929844737, 'admin', 'Chrome 14', 'Windows 10', 'Computer', '内网IP|内网IP', '127.0.0.1', 0, '登录成功', '2025-10-19 22:53:50');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '日志标题',
  `log_type` tinyint(1) NULL DEFAULT NULL COMMENT '日志类型(0插入，1更新，2删除，3导出，4导入，5其他)',
  `method_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '方法名称',
  `request_method` varchar(8) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '请求方式',
  `oper_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '操作人员',
  `oper_ip` varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '主机地点',
  `request_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `oper_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '操作地点',
  `request_param` json NULL COMMENT '请求参数',
  `response_param` json NULL COMMENT '响应参数',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '操作状态',
  `error_message` text CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) NULL DEFAULT NULL COMMENT '耗时',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (1958076554128584705, '操作日志', 6, 'com.whim.controller.system.SysOperLogController.cleanOperLog', 'DELETE', 'admin', '127.0.0.1', '/system/operLog/clean', '内网IP|内网IP', '{}', '{\"code\": 200, \"data\": null, \"message\": \"清空成功\"}', 0, NULL, '2025-08-20 16:00:04', 43);
INSERT INTO `sys_oper_log` VALUES (1958077336135593986, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', '{}', NULL, 0, NULL, '2025-08-20 16:03:10', 902);
INSERT INTO `sys_oper_log` VALUES (1958077391408132097, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', '{}', NULL, 0, NULL, '2025-08-20 16:03:24', 43);
INSERT INTO `sys_oper_log` VALUES (1958082844074635265, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', '{}', NULL, 0, NULL, '2025-08-20 16:25:04', 41);
INSERT INTO `sys_oper_log` VALUES (1958088116293824514, '字典数据', 1, 'com.whim.controller.system.SysDictDataController.editDictData', 'PUT', 'admin', '127.0.0.1', '/system/dictData/update', '内网IP|内网IP', '{\"id\": \"1958017746136088577\", \"sort\": 6, \"label\": \"清空\", \"value\": \"6\", \"remark\": \"清空\", \"dictType\": \"sys_oper_type\", \"listClass\": \"error\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-08-20 16:46:00', 1186);
INSERT INTO `sys_oper_log` VALUES (1962418869206917122, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', '{}', NULL, 0, NULL, '2025-09-01 15:34:52', 1163);
INSERT INTO `sys_oper_log` VALUES (1962419310141513730, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', '{}', NULL, 0, NULL, '2025-09-01 15:35:35', 63418);
INSERT INTO `sys_oper_log` VALUES (1962419371810365442, '操作日志', 3, 'com.whim.controller.system.SysOperLogController.exportOperLog', 'GET', 'admin', '127.0.0.1', '/system/operLog/export', '内网IP|内网IP', NULL, NULL, 0, NULL, '2025-09-01 15:36:50', 3321);
INSERT INTO `sys_oper_log` VALUES (1965418075785981953, '字典类型', 0, 'com.whim.controller.system.SysDictTypeController.addDictType', 'POST', 'admin', '127.0.0.1', '/system/dictType/add', '内网IP|内网IP', '{\"name\": \"菜单类型\", \"type\": \"sys_menu_type\", \"remark\": \"菜单类型\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 22:12:32', 7231);
INSERT INTO `sys_oper_log` VALUES (1965418209856909314, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 1, \"label\": \"目录\", \"value\": \"1\", \"remark\": \"目录\", \"dictType\": \"sys_menu_type\", \"listClass\": \"primary\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 22:13:12', 47);
INSERT INTO `sys_oper_log` VALUES (1965418352857509890, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 0, \"label\": \"菜单\", \"value\": \"2\", \"remark\": \"菜单\", \"dictType\": \"sys_menu_type\", \"listClass\": \"info\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 22:13:46', 62);
INSERT INTO `sys_oper_log` VALUES (1965418397661065217, '字典数据', 1, 'com.whim.controller.system.SysDictDataController.editDictData', 'PUT', 'admin', '127.0.0.1', '/system/dictData/update', '内网IP|内网IP', '{\"id\": \"1965418352660377602\", \"sort\": 2, \"label\": \"菜单\", \"value\": \"2\", \"remark\": \"菜单\", \"dictType\": \"sys_menu_type\", \"listClass\": \"info\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-09 22:13:56', 116);
INSERT INTO `sys_oper_log` VALUES (1965418478904733697, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 3, \"label\": \"按钮\", \"value\": \"3\", \"remark\": \"按钮\", \"dictType\": \"sys_menu_type\", \"listClass\": \"default\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 22:14:16', 32);
INSERT INTO `sys_oper_log` VALUES (1965418526480723970, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 4, \"label\": \"外链\", \"value\": \"4\", \"remark\": \"外链\", \"dictType\": \"sys_menu_type\", \"listClass\": \"default\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 22:14:27', 53);
INSERT INTO `sys_oper_log` VALUES (1965440929336336386, '字典类型', 0, 'com.whim.controller.system.SysDictTypeController.addDictType', 'POST', 'admin', '127.0.0.1', '/system/dictType/add', '内网IP|内网IP', '{\"name\": \"显示状态\", \"type\": \"sys_show_status\", \"remark\": \"显示状态\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 23:43:27', 1250);
INSERT INTO `sys_oper_log` VALUES (1965441019002167298, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 1, \"label\": \"显示\", \"value\": \"0\", \"remark\": \"显示\", \"dictType\": \"sys_show_status\", \"listClass\": \"success\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 23:43:50', 81);
INSERT INTO `sys_oper_log` VALUES (1965441085045678081, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 2, \"label\": \"隐藏\", \"value\": \"1\", \"remark\": \"隐藏\", \"dictType\": \"sys_show_status\", \"listClass\": \"error\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-09 23:44:05', 98);
INSERT INTO `sys_oper_log` VALUES (1967598783237439489, '字典类型', 1, 'com.whim.controller.system.SysDictTypeController.editDictType', 'PUT', 'admin', '127.0.0.1', '/system/dictType/update', '内网IP|内网IP', '{\"id\": \"1958014435433598977\", \"name\": \"结果状态\", \"type\": \"result_status\", \"remark\": \"结果状态\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-15 22:38:00', 1203);
INSERT INTO `sys_oper_log` VALUES (1967598831841034242, '字典类型', 1, 'com.whim.controller.system.SysDictTypeController.editDictType', 'PUT', 'admin', '127.0.0.1', '/system/dictType/update', '内网IP|内网IP', '{\"id\": \"1958014435433598977\", \"name\": \"结果状态\", \"type\": \"sys_result_status\", \"remark\": \"结果状态\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-15 22:38:12', 59);
INSERT INTO `sys_oper_log` VALUES (1967599466871242754, '字典类型', 0, 'com.whim.controller.system.SysDictTypeController.addDictType', 'POST', 'admin', '127.0.0.1', '/system/dictType/add', '内网IP|内网IP', '{\"name\": \"运行状态\", \"type\": \"sys_run_status\", \"remark\": \"运行状态\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-15 22:40:44', 52);
INSERT INTO `sys_oper_log` VALUES (1967599544109350914, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 1, \"label\": \"正常\", \"value\": \"1\", \"remark\": \"正常\", \"dictType\": \"sys_run_status\", \"listClass\": \"success\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-15 22:41:02', 88);
INSERT INTO `sys_oper_log` VALUES (1967599598652080130, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 2, \"label\": \"停止\", \"value\": \"1\", \"remark\": \"停止\", \"dictType\": \"sys_run_status\", \"listClass\": \"error\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-15 22:41:15', 234);
INSERT INTO `sys_oper_log` VALUES (1967601405126230018, '字典数据', 1, 'com.whim.controller.system.SysDictDataController.editDictData', 'PUT', 'admin', '127.0.0.1', '/system/dictData/update', '内网IP|内网IP', '{\"id\": \"1967599543778000897\", \"sort\": 1, \"label\": \"正常\", \"value\": \"0\", \"remark\": \"正常\", \"dictType\": \"sys_run_status\", \"listClass\": \"success\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-15 22:48:26', 84);
INSERT INTO `sys_oper_log` VALUES (1967602088634204161, '字典数据', 1, 'com.whim.controller.system.SysDictDataController.editDictData', 'PUT', 'admin', '127.0.0.1', '/system/dictData/update', '内网IP|内网IP', '{\"id\": \"1958015172775395330\", \"sort\": 0, \"label\": \"成功\", \"value\": \"1\", \"remark\": \"成功\", \"dictType\": \"sys_result_status\", \"listClass\": \"success\"}', NULL, 1, '字典数据键值已存在', '2025-09-15 22:51:05', 4309);
INSERT INTO `sys_oper_log` VALUES (1967602820888297473, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 0, \"label\": \"123\", \"value\": \"1\", \"remark\": \"\", \"dictType\": \"sys_result_status\", \"listClass\": \"default\"}', NULL, 1, '字典数据键值已存在', '2025-09-15 22:54:02', 990);
INSERT INTO `sys_oper_log` VALUES (1967602847878643713, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 0, \"label\": \"123\", \"value\": \"1\", \"remark\": \"\", \"dictType\": \"sys_result_status\", \"listClass\": \"default\"}', NULL, 1, '字典数据键值已存在', '2025-09-15 22:54:10', 6);
INSERT INTO `sys_oper_log` VALUES (1968323084391469057, '字典类型', 0, 'com.whim.controller.system.SysDictTypeController.addDictType', 'POST', 'admin', '127.0.0.1', '/system/dictType/add', '内网IP|内网IP', '{\"name\": \"系统菜单缓存状态\", \"type\": \"sys_menu_keepAlive_status\", \"remark\": \"系统菜单缓存状态\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-17 22:36:08', 144);
INSERT INTO `sys_oper_log` VALUES (1968323244395778050, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 1, \"label\": \"缓存\", \"value\": \"1\", \"remark\": \"缓存\", \"dictType\": \"sys_menu_keepAlive_status\", \"listClass\": \"success\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-17 22:36:46', 129);
INSERT INTO `sys_oper_log` VALUES (1968323309478793218, '字典数据', 0, 'com.whim.controller.system.SysDictDataController.addDictData', 'POST', 'admin', '127.0.0.1', '/system/dictData/add', '内网IP|内网IP', '{\"sort\": 2, \"label\": \"不缓存\", \"value\": \"0\", \"remark\": \"\", \"dictType\": \"sys_menu_keepAlive_status\", \"listClass\": \"primary\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-17 22:37:01', 70);
INSERT INTO `sys_oper_log` VALUES (1968323339577118721, '字典数据', 1, 'com.whim.controller.system.SysDictDataController.editDictData', 'PUT', 'admin', '127.0.0.1', '/system/dictData/update', '内网IP|内网IP', '{\"id\": \"1968323309218746369\", \"sort\": 2, \"label\": \"不缓存\", \"value\": \"0\", \"remark\": \"不缓存\", \"dictType\": \"sys_menu_keepAlive_status\", \"listClass\": \"info\"}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-17 22:37:08', 75);
INSERT INTO `sys_oper_log` VALUES (1969610428411191298, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1\"}', NULL, 1, 'Cannot read the array length because \"<local2>\" is null', '2025-09-21 11:51:33', 994);
INSERT INTO `sys_oper_log` VALUES (1969610569734070274, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969600597835010049\"}', NULL, 1, 'Cannot read the array length because \"<local2>\" is null', '2025-09-21 11:52:08', 1);
INSERT INTO `sys_oper_log` VALUES (1969611361996189698, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969600597835010049\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:55:16', 1402);
INSERT INTO `sys_oper_log` VALUES (1969611417046429698, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1\"}', NULL, 1, '该菜单存在子权限，请先删除子权限后再操作', '2025-09-21 11:55:26', 3867);
INSERT INTO `sys_oper_log` VALUES (1969611568276283394, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969600640369446914\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:56:05', 1170);
INSERT INTO `sys_oper_log` VALUES (1969611585653284866, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969583464870727681\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:56:10', 45);
INSERT INTO `sys_oper_log` VALUES (1969611595983855618, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969583692105535489\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:56:13', 32);
INSERT INTO `sys_oper_log` VALUES (1969611604234051585, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969583540330450946\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:56:15', 38);
INSERT INTO `sys_oper_log` VALUES (1969611631916457985, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"9\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 11:56:21', 36);
INSERT INTO `sys_oper_log` VALUES (1969617265428766722, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1969425432706703362\"}', '{\"code\": 200, \"data\": null, \"message\": \"删除成功\"}', 0, NULL, '2025-09-21 12:18:44', 108);
INSERT INTO `sys_oper_log` VALUES (1972302059065298945, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1\"}', NULL, 1, '该菜单存在子权限，请先删除子权限后再操作', '2025-09-28 22:07:08', 1125);
INSERT INTO `sys_oper_log` VALUES (1972302148085207041, '菜单管理', 2, 'com.whim.controller.system.SysPermissionController.deleteMenuByIds', 'DELETE', 'admin', '127.0.0.1', '/system/menu/delete', '内网IP|内网IP', '{\"menuIds[]\": \"1\"}', NULL, 1, '该菜单存在子权限，请先删除子权限后再操作', '2025-09-28 22:07:30', 5);
INSERT INTO `sys_oper_log` VALUES (1972309779252236290, '菜单管理', 0, 'com.whim.controller.system.SysPermissionController.addMenu', 'POST', 'admin', '127.0.0.1', '/system/menu/add', '内网IP|内网IP', '{\"code\": \"\", \"icon\": \"user-filled\", \"name\": \"/system/role\", \"path\": \"/system/role/index\", \"sort\": 0, \"type\": 1, \"title\": \"角色管理\", \"remark\": \"角色管理\", \"status\": 0, \"visible\": 0, \"parentId\": \"1\", \"redirect\": \"\", \"component\": \"\", \"keepAlive\": 0, \"queryParam\": \"\"}', '{\"code\": 200, \"data\": null, \"message\": \"添加成功\"}', 0, NULL, '2025-09-28 22:37:50', 126);
INSERT INTO `sys_oper_log` VALUES (1972309949176074242, '菜单管理', 1, 'com.whim.controller.system.SysPermissionController.updateMenu', 'PUT', 'admin', '127.0.0.1', '/system/menu/update', '内网IP|内网IP', '{\"id\": \"1972309778769891329\", \"code\": \"\", \"icon\": \"user-filled\", \"name\": \"SysRole\", \"path\": \"/system/role\", \"sort\": 0, \"type\": 2, \"title\": \"角色管理\", \"remark\": \"角色管理\", \"status\": 0, \"visible\": 0, \"parentId\": \"1\", \"redirect\": \"\", \"component\": \"/system/role/index\", \"keepAlive\": 0, \"queryParam\": null}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-28 22:38:30', 228);
INSERT INTO `sys_oper_log` VALUES (1972312476508405761, '菜单管理', 1, 'com.whim.controller.system.SysPermissionController.updateMenu', 'PUT', 'admin', '127.0.0.1', '/system/menu/update', '内网IP|内网IP', '{\"id\": \"1972309778769891329\", \"code\": null, \"icon\": \"user-filled\", \"name\": \"SysRole\", \"path\": \"/system/role\", \"sort\": 0, \"type\": 2, \"title\": \"角色管理\", \"remark\": \"角色管理\", \"status\": 0, \"visible\": 0, \"parentId\": \"1\", \"redirect\": null, \"component\": \"/system/role/index\", \"keepAlive\": 0, \"queryParam\": null}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-28 22:48:32', 1283);
INSERT INTO `sys_oper_log` VALUES (1972312511551815681, '菜单管理', 1, 'com.whim.controller.system.SysPermissionController.updateMenu', 'PUT', 'admin', '127.0.0.1', '/system/menu/update', '内网IP|内网IP', '{\"id\": \"1972309778769891329\", \"code\": \"\", \"icon\": \"user-filled\", \"name\": \"SysRole\", \"path\": \"/system/role\", \"sort\": 0, \"type\": 2, \"title\": \"角色管理\", \"remark\": \"角色管理\", \"status\": 0, \"visible\": 0, \"parentId\": \"1\", \"redirect\": \"\", \"component\": \"/system/role/index\", \"keepAlive\": 0, \"queryParam\": null}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-28 22:48:41', 73);
INSERT INTO `sys_oper_log` VALUES (1972313435309522945, '菜单管理', 1, 'com.whim.controller.system.SysPermissionController.updateMenu', 'PUT', 'admin', '127.0.0.1', '/system/menu/update', '内网IP|内网IP', '{\"id\": \"2\", \"code\": null, \"icon\": \"yonghu\", \"name\": \"SysUser\", \"path\": \"user\", \"sort\": 2, \"type\": 2, \"title\": \"用户管理\", \"remark\": \"用户管理\", \"status\": 0, \"visible\": 0, \"parentId\": \"1\", \"redirect\": null, \"component\": \"system/user/index\", \"keepAlive\": 1, \"queryParam\": null}', '{\"code\": 200, \"data\": null, \"message\": \"修改成功\"}', 0, NULL, '2025-09-28 22:52:21', 78);
INSERT INTO `sys_oper_log` VALUES (1972649148957835265, '字典类型', 6, 'com.whim.controller.system.SysDictTypeController.resetDictCache', 'DELETE', 'admin', '127.0.0.1', '/system/dictType/reset', '内网IP|内网IP', NULL, '{\"code\": 200, \"data\": null, \"message\": \"重置成功\"}', 0, NULL, '2025-09-29 21:06:21', 1080);
INSERT INTO `sys_oper_log` VALUES (1972835645874970625, '字典类型', 6, 'com.whim.controller.system.SysDictTypeController.resetDictCache', 'DELETE', 'admin', '127.0.0.1', '/system/dictType/reset', '内网IP|内网IP', NULL, '{\"code\": 200, \"data\": null, \"message\": \"重置成功\"}', 0, NULL, '2025-09-30 09:27:25', 1191);
INSERT INTO `sys_oper_log` VALUES (1972959564363640834, '字典类型', 6, 'com.whim.controller.system.SysDictTypeController.resetDictCache', 'DELETE', 'admin', '127.0.0.1', '/system/dictType/reset', '内网IP|内网IP', NULL, '{\"code\": 200, \"data\": null, \"message\": \"重置成功\"}', 0, NULL, '2025-09-30 17:39:49', 1124);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(20) NOT NULL COMMENT '权限ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由名称',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父权限ID(0表示根节点)',
  `type` tinyint(1) NOT NULL COMMENT '类型(1-目录 2-菜单 3-按钮 4-外链)',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `query_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `keep_alive` tinyint(1) NOT NULL DEFAULT 0 COMMENT '页面是否缓存(0不缓存，1缓存)',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '显示排序(升序)',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识(如:system:user:add)',
  `visible` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可见(0-显示 1-隐藏)',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标名称',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '重定向路径',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`code` ASC) USING BTREE COMMENT '权限标识唯一索引',
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '父权限索引',
  INDEX `idx_type`(`type` ASC) USING BTREE COMMENT '类型索引',
  INDEX `idx_sort`(`sort` ASC) USING BTREE COMMENT '排序索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表(菜单和按钮权限)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'System', '系统管理', 0, 1, 'system', NULL, '', 0, 0, NULL, 0, 0, 'setting', NULL, '系统管理', 1, '2025-08-11 19:21:58', NULL, '2025-08-11 19:21:58', 0);
INSERT INTO `sys_permission` VALUES (2, 'SysUser', '用户管理', 1, 2, 'user', NULL, 'system/user/index', 1, 2, NULL, 0, 0, 'yonghu', NULL, '用户管理', 1, '2025-09-28 22:52:21', 1, '2025-09-28 22:52:21', 0);
INSERT INTO `sys_permission` VALUES (3, 'SysDictType', '字典管理', 1, 2, 'dictType', NULL, 'system/dictType/index', 0, 3, NULL, 0, 0, 'zidianmokuai', NULL, '字典管理', 1, '2025-09-09 17:51:50', NULL, '2025-09-09 17:51:50', 0);
INSERT INTO `sys_permission` VALUES (4, 'SysDictData', '字典数据', 1, 2, 'dictData/:type', NULL, 'system/dictData/index', 0, 0, NULL, 1, 0, 'zidianmokuai', NULL, '字典数据管理', 1, '2025-08-14 20:06:39', NULL, '2025-08-14 20:06:39', 0);
INSERT INTO `sys_permission` VALUES (5, 'SysLog', '日志管理', 1, 1, 'log', NULL, '', 0, 4, NULL, 0, 0, 'rizhi', NULL, '日志管理', 1, '2025-09-09 17:51:52', NULL, '2025-09-09 17:51:52', 0);
INSERT INTO `sys_permission` VALUES (6, 'SysLoginLog', '登录日志', 5, 2, 'loginLog', NULL, 'system/loginLog/index', 0, 0, NULL, 0, 0, 'denglurizhi', NULL, '登录日志', 1, '2025-08-18 16:20:28', NULL, '2025-08-18 16:20:28', 0);
INSERT INTO `sys_permission` VALUES (7, 'SysOperLog', '操作日志', 5, 2, 'operLog', NULL, 'system/operLog/index', 0, 0, NULL, 0, 0, 'wj-rz', NULL, '操作日志', 1, '2025-08-20 15:41:56', NULL, '2025-08-20 15:41:56', 0);
INSERT INTO `sys_permission` VALUES (8, '', '用户查询', 2, 3, NULL, NULL, NULL, 0, 1, 'system:user:query', 0, 0, NULL, NULL, '用户查询', 1, '2025-09-09 23:25:54', NULL, '2025-09-09 23:25:54', 0);
INSERT INTO `sys_permission` VALUES (10, 'SysPermission', '菜单管理', 1, 2, 'menu', NULL, 'system/menu/index', 0, 1, NULL, 0, 0, 'menu', NULL, '菜单管理', 1, '2025-09-13 14:55:37', NULL, '2025-09-13 14:55:37', 0);
INSERT INTO `sys_permission` VALUES (1972309778769891329, 'SysRole', '角色管理', 1, 2, '/system/role', '', '/system/role/index', 0, 0, '', 0, 0, 'user-filled', '', '角色管理', 1, '2025-09-28 22:48:41', 1, '2025-09-28 22:48:41', 0);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint(20) NOT NULL COMMENT '岗位ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `code` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '岗位编码',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '岗位名称',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `status` tinyint(1) NOT NULL COMMENT '状态(0启用，1禁用)',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(255) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint(1) NOT NULL COMMENT '删除标志(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE COMMENT '部门ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '岗位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL COMMENT '角色ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `data_scope` tinyint(1) NOT NULL DEFAULT 1 COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE COMMENT '角色名称唯一索引',
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE COMMENT '角色编码唯一索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'superadmin', 1, '超级管理员', 0, '超级管理员', 1, '2025-07-18 23:44:24', NULL, '2025-07-18 23:44:24', 0);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `dept_id` bigint(20) NOT NULL,
  `create_by` bigint(255) NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint(20) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_dept`(`role_id` ASC, `dept_id` ASC) USING BTREE COMMENT '角色-部门唯一索引',
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE COMMENT '部门ID索引',
  CONSTRAINT `fk_rd_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rd_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '角色与部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (1, 1, 2, 1, '2025-07-10 16:21:39', NULL, '2025-07-10 16:21:39');
INSERT INTO `sys_role_dept` VALUES (2, 1, 3, 1, '2025-07-16 01:08:15', NULL, '2025-07-16 01:08:15');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(20) NOT NULL COMMENT '关联ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE COMMENT '角色-权限唯一索引',
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE COMMENT '权限ID索引',
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL COMMENT '用户ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密存储)',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `name` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号码',
  `gender` tinyint(1) NOT NULL DEFAULT 0 COMMENT '性别(0-未知 1-男 2-女)',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE COMMENT '用户名唯一索引',
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE COMMENT '邮箱唯一索引',
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE COMMENT '手机号唯一索引',
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE COMMENT '部门ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '$2a$10$XAnymnSBtVxlnwLEqLMFOeoIUmUQhhnRmszGC3Ro.neGgPUjUpvuC', NULL, '管理员', 'jince_hm@163.com', '18331312122', 1, 0, '管理员', 1, '2025-06-27 18:12:07', 1, '2025-06-27 18:12:20', 0);
INSERT INTO `sys_user` VALUES (2, 2, 'test', '$2a$10$XAnymnSBtVxlnwLEqLMFOeoIUmUQhhnRmszGC3Ro.neGgPUjUpvuC', NULL, '管理员', 'test@163.com', '18331312123', 1, 0, '管理员', 1, '2025-07-16 00:42:00', 1, '2025-07-16 00:42:00', 0);

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE COMMENT '用户-岗位唯一索引',
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE COMMENT '岗位id索引'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '用户岗位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL COMMENT '关联ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE COMMENT '用户-角色唯一索引',
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE COMMENT '角色ID索引',
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, 1, '2025-07-08 19:38:48', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
