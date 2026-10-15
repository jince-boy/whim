---
title: 启动文档站
description: 安装、命令、目录维护和静态部署。
---

# 启动文档站

文档站是 `whim/whim-docs` 下的独立项目。它不启动管理端或后端，不需要数据库、Redis 和登录会话。

## 安装与运行

使用 Node.js 22+ 和项目 `package.json` 中指定的 pnpm。在 `whim-docs` 目录执行：

```sh
pnpm install
pnpm dev
```

访问 `http://127.0.0.1:5175`。开发和预览默认只监听本机，且启用严格端口检查，不会在端口冲突时静默换端口。

## 可用命令

| 命令              | 作用                           |
| ----------------- | ------------------------------ |
| `pnpm dev`        | 开发模式与 Markdown 热更新     |
| `pnpm type-check` | 检查文档主题和演示组件类型     |
| `pnpm build`      | 静态生成全部文档并检查站内链接 |
| `pnpm preview`    | 在 `4175` 端口预览构建结果     |
| `pnpm format`     | 格式化站点源码和文档           |

## 主题如何与管理端保持一致

文档站从 `whim-vue/src/config/NaiveTheme.ts` 导入基础主色、浅色和暗色覆盖配置。基础绿色为 `#4f8466`，浅色背景为 `#f6f8f4`，暗色使用相同的提亮规则。字体从管理端本地字体资源引入。

文档导航、抽屉、搜索弹窗、主题开关、消息与演示卡片均使用 Naive UI。Naive 样式在静态渲染期间按页面收集并写入 HTML；正文无需等待客户端运行才显示。

## 如何新增文档

1. 在 `guide`、`components` 或 `backend` 中增加 Markdown。
2. 设置 `title` 和 `description`。
3. 将页面加入 `.vitepress/navigation.ts`。
4. 运行类型检查与构建，确认链接和演示可用。

正文会自动进入本站全文搜索索引；搜索支持中文、英文与空格分隔的多个关键词。按 `Ctrl K` 或 `Command K` 打开搜索。右侧页内目录来自二级、三级标题。

## 静态部署

构建输出位于 `.vitepress/dist`。部署到域名根路径时直接托管该目录。部署到 `/whim/` 等子路径时，在构建前设置：

```powershell
$env:DOCS_BASE = '/whim/'
pnpm build
```

内部链接通过 `withBase` 或 VitePress Markdown 转换处理。静态服务器需要支持无后缀路径对应 `.html` 文件，并返回生成的 `404.html` 处理缺页。

## 源码演示

图标、图标选择、查询表单和弹窗直接引用管理端源码。客户端交互通过 `ClientOnly` 隔离，避免 DOM 操作进入静态渲染。保留 `whim-vue` 与 `whim-docs` 的相邻目录结构。

表格、菜单搜索和主题抽屉依赖管理端的 store、路由或授权状态，文档提供真实接入示例和边界说明，不伪造同名组件来冒充演示。
