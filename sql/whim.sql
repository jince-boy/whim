/*
 Navicat Premium Dump SQL

 Source Server         : phpstudy
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : whim2

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 15/04/2026 23:16:43
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`
(
    `id`              bigint(20) NOT NULL COMMENT 'id',
    `user_id`         bigint(20) NOT NULL COMMENT '租户管理员ID',
    `tenant_code`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '租户编码',
    `company_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
    `contact_person`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '联系人',
    `contact_number`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '联系电话',
    `company_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业地址',
    `company_code`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业统一社会信用代码',
    `package_id`      bigint(20) NULL DEFAULT NULL COMMENT '套餐ID',
    `status`          tinyint(1) NULL DEFAULT 0 COMMENT '租户状态(0正常 1停用)',
    `expire_time`     datetime NULL DEFAULT NULL COMMENT '过期时间',
    `account_count`   int(11) NULL DEFAULT 0 COMMENT '用户限额(0不限制)',
    `domain`          varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定域名',
    `remark`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
    `create_by`       bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`       bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`       bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time`     datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`         tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统租户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `username`    varchar(32) CHARACTER SET utf8 COLLATE utf8_unicode_ci  NOT NULL COMMENT '用户名',
    `password`    varchar(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '密码(加密存储)',
    `avatar`      varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
    `name`        varchar(24) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
    `email`       varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '电子邮箱',
    `phone`       varchar(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '手机号码',
    `gender`      tinyint(1) NULL DEFAULT 0 COMMENT '性别(0-未知 1-男 2-女)',
    `status`      tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
    `remark`      varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '备注',
    `create_by`   bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`   bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tenant`;
CREATE TABLE `sys_user_tenant`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户ID',
    `tenant_id`   bigint(20) NOT NULL COMMENT '租户ID',
    `create_by`   bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`   bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户租户关联表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for sys_tenant_package
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_package`;
CREATE TABLE `sys_tenant_package`
(
    `id`           bigint(20) NOT NULL COMMENT 'id',
    `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
    `sort`         int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `status`       tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-正常 1-停用)',
    `remark`       varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_by`    bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`    bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`    bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time`  datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`      tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统租户套餐表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `role_name`   varchar(32)  NOT NULL COMMENT '角色名称',
    `role_code`   varchar(128) NOT NULL COMMENT '角色编码',
    `role_type`   tinyint(1) NOT NULL DEFAULT 0 COMMENT '角色类型(0-系统 1-租户)',
    `data_scope`  tinyint(1) NOT NULL DEFAULT 1 COMMENT '数据范围(1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限)',
    `sort`        int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `tenant_id`   bigint(20) NULL DEFAULT NULL COMMENT '租户ID',
    `status`      tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-正常 1-停用)',
    `remark`      varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_by`   bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`   bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户ID',
    `role_id`     bigint(20) NOT NULL COMMENT '角色ID',
    `tenant_id`   bigint(20) NOT NULL COMMENT '租户ID',
    `create_by`   bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`   bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`          bigint(20) NOT NULL COMMENT 'id',
    `route_name`  varchar(64)  NOT NULL COMMENT '路由名称',
    `menu_name`   varchar(64)  NOT NULL COMMENT '菜单名称',
    `perms`       varchar(128) NOT NULL COMMENT '权限标识',
    `parent_id`   bigint(20) NOT NULL DEFAULT 0 COMMENT '父级ID(0为顶级菜单)',
    `menu_type`   tinyint(1) NOT NULL DEFAULT 0 COMMENT '类型(1-目录 2-菜单 3-按钮 4-外链)',
    `path`        varchar(255) NULL DEFAULT NULL COMMENT '前端路由路径',
    `param`       varchar(255) NULL DEFAULT NULL COMMENT '路由参数',
    `component`   varchar(255) NULL DEFAULT NULL COMMENT '前端组件路径',
    `is_cache`    tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否缓存(0-不缓存 1-缓存)',
    `sort`        int(11) NOT NULL DEFAULT 0 COMMENT '排序',
    `icon`        varchar(100) NULL DEFAULT NULL COMMENT '图标',
    `visible`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '可见性(0-显示 1-隐藏)',
    `status`      tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0-正常 1-停用)',
    `remark`      varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_by`   bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `delete_by`   bigint(20) NULL DEFAULT NULL COMMENT '删除人ID',
    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志(0-未删除 1-已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限菜单表' ROW_FORMAT = DYNAMIC;

SET
FOREIGN_KEY_CHECKS = 1;
