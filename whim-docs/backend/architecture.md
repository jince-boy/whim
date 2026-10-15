---
title: 架构与接入
description: 后端模块职责、自动配置与业务接入的完整路径。
---

# 架构与接入

后端采用 Maven 多模块结构。`whim-start` 负责组装与启动，公共基础能力通过自动配置提供，业务逻辑放入对应领域模块。

## 模块职责

| 模块               | 职责与主要入口                                |
| ------------------ | --------------------------------------------- |
| `whim-core`        | 认证抽象、数据范围抽象、工具和基础异常        |
| `whim-json`        | Jackson 配置                                  |
| `whim-web`         | `Result`、异常映射、MVC 转换、XSS 过滤        |
| `whim-redis`       | Redisson 与缓存配置                           |
| `whim-satoken`     | 认证实现、系统组合注解、Token-Session、拦截器 |
| `whim-mybatisplus` | 持久化插件、分页、数据权限、审计填充          |
| `whim-system`      | 系统用户、部门、角色、权限、岗位等业务        |
| `whim-start`       | 启动、Controller、应用配置与 Flyway           |

基线为 JDK 25+、Spring Boot 4.1+。本次文档建设不执行 Java 或 Maven 命令。

## 依赖边界

```text
                     whim-start
                    /          \
            whim-satoken      whim-system
                    \          /       \
                     whim-core       whim-mybatisplus
                         \               /
                       公共抽象与基础能力
```

图示表达主要组合关系，并非完整 Maven 依赖图。`whim-satoken` 和 `whim-mybatisplus` 为独立兄弟模块，都可以依赖 core，但不能互相依赖。业务模块使用 `AuthenticationContext` 与 `AuthenticationSession`，不直接依赖具体的 Sa-Token 实现。

## 自动配置

框架模块通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置。排查某项 Bean 时，先查看对应基础模块的自动配置，不要默认所有 Bean 都声明在启动模块。

认证模块注册上下文、会话和权限适配；MyBatis-Plus 配置数据权限、分页、乐观锁与防全表写插件。数据权限执行在分页之前，保证行数据与总数使用同一范围。

## 新增业务能力

1. 确定领域。系统管理放在 `whim-system`；其他领域可以创建独立业务模块。
2. 在业务模块创建 Entity、DTO、VO、Mapper、Service 与实现。
3. 在 `whim-start/controller/<业务>` 创建 Controller，保持路由与模块组织各自清晰。
4. 在权限目录登记精确的操作码；需要数据保护的操作启用 `data_permission`。
5. Controller 检查功能权限，公开 Service 方法声明数据权限映射。
6. 新建或归属迁移显式校验业务归属；多表写入在 Service 定义事务。
7. 数据库变更使用新的更高 Flyway 版本，缓存与会话变更协调到事务提交后。

## 层次分工

| 层         | 应负责                                 | 不应承担                         |
| ---------- | -------------------------------------- | -------------------------------- |
| Controller | 路由、参数校验、功能授权、统一返回     | SQL 拼接、业务事务               |
| Service    | 业务流程、精确操作范围、归属校验、事务 | 自建另一套错误响应               |
| Mapper     | 持久化映射，必要时复杂 SQL             | 决定当前用户属于什么角色         |
| 框架       | 通用认证、数据范围注入、异常、审计     | 特定业务的审批或负责人有效性规则 |

## 相关入口

- [功能权限与 Sa-Token](/backend/satoken)
- [记录级数据权限](/backend/data-permission)
- [会话与授权变更](/backend/session)
- [核心工具与 Web 约定](/backend/core)

源码根目录：`whim-framework`、`whim-modules`、`whim-start`。
