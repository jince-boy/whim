---
title: 开发约定
description: 前端页面组织、组件通信与后端服务边界。
---

# 开发约定

完整规范以仓库根目录 `AGENTS.md`、`whim-vue/AGENTS.md` 为准。本页整理影响组件接入和业务开发的主要约定。

## 前端文件职责

```text
src/views/system/example/
├── index.vue
├── form.vue
└── hooks/
    ├── useExample.ts
    ├── useExampleForm.ts
    ├── api.ts
    ├── types.ts
    └── rule.ts
```

只创建实际需要的文件。`index.vue` 组织页面结构，`form.vue` 组织编辑字段，hook 管理状态、查询和操作。功能接口、类型与校验就近维护。公共组件位于 `src/components`，独立逻辑位于所属组件的 `hooks`。

## 组件通信

只读输入使用类型式 `defineProps`；普通操作通知使用命名元组 `defineEmits`；实时双向值使用 `defineModel`。父组件模板采用 kebab-case 的属性与事件名。

编辑表单使用 `initialModel` 创建独立草稿，取消后丢弃。命令式表单弹窗通过 `defineExpose({ formRef, formModel })` 读取校验实例与提交模型。静态模板引用使用 `useTemplateRef`。

注册式组件每个 hook 只连接一个实例。表格和查询表单的状态由 hook 持有，通过 `@register` 连接组件，不在页面重复绑定加载、分页和查询对象。

## 服务与持久化

普通操作优先使用 MyBatis-Plus `IService` 与条件构造器，复杂联查才写 Mapper XML。多表原子操作在 Service 定义事务。事务相关缓存失效与会话撤销应在提交成功后执行。

新建和归属变更不仅需要功能授权，也需要显式校验新记录的业务归属。审计字段与业务负责人字段不同，见[数据权限](/backend/data-permission)。

## 认证与模块依赖

业务模块依赖 `whim-core` 的认证抽象。`whim-satoken` 和 `whim-mybatisplus` 是独立基础模块，由 `whim-start` 组装。不要让持久化模块直接调用 Sa-Token，也不要让认证模块依赖 MyBatis-Plus。

## 接口与数据库

Controller 位于 `whim-start/src/main/java/com/whim/controller`，按业务领域分组。静态路由段使用 lower camel case，例如 `/userInfo`。普通响应使用 `Result<T>`，分页使用 `PageQueryDTO` 和 `PageDataVO<T>`，请求校验使用 `jakarta.validation`。

数据库结构变化创建更高版本 Flyway 迁移，不修改已应用的版本脚本。接口契约变化需同步受影响的 Apifox API；内部实现、组件样式和文档站不触发 API 同步。

## 代码风格

Java 基线为 JDK 25+、Spring Boot 4.1+。使用 Lombok、`@RequiredArgsConstructor`、`jakarta.*`，不使用 `record`。日志、异常和必要的方法注释使用中文。优先复用现有工具与清晰直接的逻辑。
