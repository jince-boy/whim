# whim-vue

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

使用 Node.js 26 和 pnpm 12.8.1。`package.json` 的 `packageManager` 字段锁定 pnpm 版本。

```sh
pnpm install
```

### 环境配置

首次运行前，将 `.env.example` 复制为 `.env`，然后按需修改：

```powershell
Copy-Item -LiteralPath .env.example -Destination .env
```

| 变量 | 用途 |
| --- | --- |
| `VITE_PORT` | 开发服务器端口 |
| `VITE_API_BASE_URL` | 接口公共路径前缀，Axios 和开发代理统一使用 |
| `VITE_USE_MOCK` | 可选：`true` 使用本地测试数据，`false` 请求后端；未配置时开发环境启用、生产构建关闭 |
| `VITE_APP_TITLE` | 浏览器标签、登录页、侧栏标题和默认水印 |
| `VITE_APP_DESCRIPTION` | 登录页介绍和 HTML 页面描述 |
| `VITE_APP_COPYRIGHT` | 登录页和页面底部的版权文案 |

标题、描述、版权和接口前缀必须填写。仓库只提交 `.env.example`；`.env`、`.env.development`、`.env.production` 和其他 `.env.*` 文件均为本地配置，不提交到 Git。需要按环境覆盖时，在对应模式的文件中配置同名变量即可。

`VITE_API_BASE_URL` 默认配置为 `/api`，必须以 `/` 开头，不能带末尾 `/`、查询参数或片段。业务接口只填写 `/system/...`，公共 Axios 实例为普通请求和文件下载统一添加前缀。例如修改为 `/gateway` 后，登录请求会访问 `/gateway/system/auth/login`。开发代理自动匹配该前缀并将其剥离后转发到 `http://localhost:8089`；生产环境的反向代理也需要匹配配置的前缀并剥离，再转发到后端的 `/system/...` 路径。

环境变量在开发服务器启动或构建时读取；修改后需要重启开发服务器或重新构建。`VITE_` 变量会进入浏览器端构建产物，应只填写公开配置。详见 [Vite 环境变量文档](https://vite.dev/guide/env-and-mode)。用户已保存的自定义水印继续生效，恢复默认主题时使用新的项目标题。

### 使用本地 JSON 开发前端

运行 `pnpm dev` 时默认启用本地 Mock，无需启动后端。若本地环境文件已配置 `VITE_USE_MOCK=false`，改为 `true` 后重启开发服务器即可。登录页会显示测试账号：`admin` / `admin123`，验证码图片为 `1 + 2 = ?`，输入 `3`。

测试数据位于 `src/mock/data/`：

| 文件 | 内容 |
| --- | --- |
| `auth.json` | 测试账号、用户信息、角色和权限 |
| `menus.json` | 平铺菜单、页面路由和按钮权限 |
| `dictTypes.json` / `dictData.json` | 字典类型、状态和下拉选项 |
| `loginLogs.json` / `operLogs.json` | 日志列表、成功失败状态和详情 |

Mock 覆盖现有登录、退出、用户信息、菜单、字典和日志接口，保留 `{ code, message, data }` 响应结构。菜单树、条件搜索、分页、详情、新增、修改、单条或批量删除、日志清空均在浏览器内完成。导出会下载真实的 UTF-8 CSV 文件，便于检查导出交互。用户管理页面目前仅为占位页面，不额外增加接口或业务功能。

新增、修改和删除保存到当前标签页的 `sessionStorage`，刷新页面后继续保留。修改 JSON 初始数据后，若需要重新加载初始值，在浏览器控制台执行 `sessionStorage.removeItem('whim:mock:database:v1')` 并刷新；这也可以恢复被删除的菜单和字典。页面表单不会直接修改 JSON 或模拟数据库中的对象。

所有请求（包括下载）通过公共 Axios 适配器处理，Mock 开启时不会发往后端，也不会在未匹配接口时回退到真实请求。后续新增接口时，在 `src/mock/` 对应处理模块中补充模拟逻辑及 JSON 数据；缺失的模拟接口会明确报错。

后端联调时在本地 `.env.development.local` 设置 `VITE_USE_MOCK=false`，重启开发服务器并重新登录。需要离线演示构建时，可在 `.env.production.local` 设置 `VITE_USE_MOCK=true`；普通生产构建默认使用真实接口。模式切换后需重新登录，避免混用测试令牌和真实令牌。

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Type-Check, Compile and Minify for Production

```sh
pnpm build
```

TypeScript 7 用于检查 Node 配置文件；Vue 单文件组件和 ESLint 使用微软官方的 TypeScript 6 兼容包。两者通过 npm 别名同时安装，以兼容依赖编译器 API 的工具。参见 [TypeScript 官方说明](https://devblogs.microsoft.com/typescript/announcing-typescript-7-0/#running-side-by-side-with-typescript-60)。

### Lint with [ESLint](https://eslint.org/)

```sh
pnpm lint
```

### 接口代码组织

业务接口请求按使用功能就近放置；`src/api/index.ts` 只维护公共 Axios 请求封装和统一响应类型：

- 页面接口放在对应页面的 `hooks/api.ts`，请求和响应类型放在同目录的 `types.ts`。
- 字典类型、字典数据、菜单、登录日志和操作日志各自维护接口；登录接口位于 `src/views/login/hooks/api.ts`。
- 当前用户信息由用户 store 使用，接口和类型位于 `src/stores/modules/user/`。
- 多处使用的同一个接口从所属功能的 `api.ts` 复用，例如字典选择组件复用 `src/views/system/dictData/hooks/api.ts`。

公共 HTTP 请求、拦截器、文件下载和 `ApiResponse<T>` 由 `src/api/index.ts` 提供，业务接口通过 `@/api` 引用。`request()` 返回统一响应体，Axios 实例保留完整响应；HTTP 401 和业务码 401 会清理登录状态并跳转到登录页，其他业务码仍由调用方处理。下载会拦截 JSON 格式的业务错误，并支持 `Content-Disposition` 的 UTF-8、带引号和不带引号的文件名。
