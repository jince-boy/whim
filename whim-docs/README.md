# Whim 文档站

独立的 VitePress 项目。主题布局由 Naive UI 构建，主色、基础主题和字体直接复用 `whim-vue` 源码，不修改管理端的业务运行时。

## 环境与启动

在 `whim-docs` 目录执行，Node.js 22+，使用项目 `packageManager` 指定的 pnpm：

```sh
pnpm install
pnpm dev
```

默认地址 `http://127.0.0.1:5175`，只绑定本机并严格检查端口。端口被占用时停止旧实例或显式指定其他端口。

```sh
pnpm type-check
pnpm build
pnpm preview
```

静态产物为 `.vitepress/dist`。站点运行不依赖后端、Redis、数据库或登录账号。

## 目录

| 路径                       | 职责                                   |
| -------------------------- | -------------------------------------- |
| `guide`                    | 项目介绍、启动与开发约定               |
| `components`               | 全部自定义组件说明                     |
| `backend`                  | 数据权限、注解、认证、多账号与核心能力 |
| `.vitepress/config.ts`     | VitePress 与 Naive UI SSR 配置         |
| `.vitepress/navigation.ts` | 唯一的导航与相邻页面配置               |
| `.vitepress/theme`         | Naive UI 主题、搜索、布局与源码演示    |

## 维护方式

新增页面时设置 `title`、`description`，并加入 `navigation.ts`。全文搜索会在构建时自动索引 `guide`、`components`、`backend` 的正文。构建启用死链检查，不使用 `ignoreDeadLinks` 隐藏缺页。

交互演示直接引用 `../whim-vue/src` 的真实实现。保留此仓库的相邻目录结构；无需复制组件。涉及应用 store 和 vue-router 的组件提供完整接入示例，不在文档站伪造应用环境。

部署到子路径时，先设置 `DOCS_BASE`，值以 `/` 开头和结尾。例如 PowerShell：

```powershell
$env:DOCS_BASE = '/whim/'
pnpm build
```

将 `.vitepress/dist` 上传到静态托管即可。`cleanUrls` 启用，服务器应将 `/path` 映射到 `/path.html`，并使用生成的 `404.html` 处理缺页。无需业务 API 代理。

现有仓库 `docs/` 保留为设计与历史资料。本站以当前源码为准，不将早期租户规划、历史验收数量或未实现的认证流程当成现有能力。
