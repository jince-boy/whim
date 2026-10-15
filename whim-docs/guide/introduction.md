---
title: 项目介绍
description: Whim 的模块边界、前后端能力与阅读路径。
---

# 项目介绍

Whim 是一个 Vue 3 + Naive UI 管理端与 Java / Spring Boot 后端组成的项目。文档围绕仓库中已经存在的能力编写：如何使用组件、如何保护服务数据，以及如何扩展认证体系。

## 仓库结构

```text
whim/
├── whim-vue/         # Vue 管理端
├── whim-docs/        # 当前文档站
├── whim-framework/   # 公共基础设施与自动配置
├── whim-modules/     # 业务模块，目前为 whim-system
├── whim-start/       # Spring Boot 组装入口与 Controller
└── docs/             # 设计、历史验收与 OpenAPI 资料
```

## 前端能力

| 能力     | 入口                                                          | 适用场景                               |
| -------- | ------------------------------------------------------------- | -------------------------------------- |
| 数据表格 | [WhimTable](/components/table)                                | 分页、查询、工具栏、选择、列设置、全屏 |
| 查询表单 | [SearchForm](/components/search-form)                         | 独立查询条件与注册式表单               |
| 通用弹窗 | [useFormDialog](/components/dialog)                           | 编辑草稿、确认、删除与异步提交         |
| 图标系统 | [Icon](/components/icon)、[图标选择](/components/select-icon) | Iconfont 渲染与单选                    |
| 导航搜索 | [菜单搜索](/components/menu-search)                           | 在当前可见、可访问菜单中导航           |
| 外观配置 | [主题设置](/components/theme)                                 | 布局、主题、标签页与表格样式           |

## 后端能力

框架采用职责分离的模块结构。业务代码通过 `AuthenticationContext`、`AuthenticationSession` 访问认证能力，通过 `@DataPermission` 声明操作与表映射。Sa-Token 和 MyBatis-Plus 分别提供实现，不互相依赖。

| 想解决的问题                 | 对应文档                              |
| ---------------------------- | ------------------------------------- |
| 当前用户是谁，怎么登录和退出 | [会话与授权变更](/backend/session)    |
| 某个接口需要什么权限         | [Sa-Token 权限注解](/backend/satoken) |
| 同一个接口可以访问哪些记录   | [数据权限](/backend/data-permission)  |
| 项目有哪些自定义注解         | [自定义注解](/backend/annotations)    |
| 如何增加独立的账号体系       | [多账号认证](/backend/multi-account)  |

## 当前能力边界

目前落地的是单组织系统账号体系 `system`。系统用户具备一个主部门、多个角色和多个岗位；岗位不会隐式授予角色。数据权限包含全部、自定义部门、本部门、本部门及下级、本人五种范围。

多账号认证已有集中注册和分派入口，实际仅注册 `SYSTEM`。会员、商家等体系需要业务接入；多租户、完整二次认证流程不属于当前现成能力。历史资料中的规划不等同于已实现功能。

## 推荐阅读顺序

先了解[开发约定](/guide/conventions)，前端从[组件总览](/components/overview)选择入口；后端先看[架构与接入](/backend/architecture)，再分别理解功能权限与数据权限。两种权限解决的问题不同，需要配合使用。
