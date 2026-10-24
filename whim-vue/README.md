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
| `VITE_APP_TITLE` | 浏览器标签、登录页、侧栏标题和默认水印 |
| `VITE_APP_DESCRIPTION` | 登录页介绍和 HTML 页面描述 |
| `VITE_APP_COPYRIGHT` | 登录页和页面底部的版权文案 |

标题、描述、版权和接口前缀必须填写。仓库只提交 `.env.example`；`.env`、`.env.development`、`.env.production` 和其他 `.env.*` 文件均为本地配置，不提交到 Git。需要按环境覆盖时，在对应模式的文件中配置同名变量即可。

`VITE_API_BASE_URL` 默认配置为 `/api`，必须以 `/` 开头，不能带末尾 `/`、查询参数或片段。业务接口只填写 `/system/...`，公共 Axios 实例为普通请求和文件下载统一添加前缀。例如修改为 `/gateway` 后，登录请求会访问 `/gateway/system/auth/login`。开发代理自动匹配该前缀并将其剥离后转发到 `http://localhost:8089`；生产环境的反向代理也需要匹配配置的前缀并剥离，再转发到后端的 `/system/...` 路径。

环境变量在开发服务器启动或构建时读取；修改后需要重启开发服务器或重新构建。`VITE_` 变量会进入浏览器端构建产物，应只填写公开配置。详见 [Vite 环境变量文档](https://vite.dev/guide/env-and-mode)。用户已保存的自定义水印继续生效，恢复默认主题时使用新的项目标题。

### 真实后端登录

开发和生产环境的业务请求均连接真实后端，本地开发前需要启动后端及其依赖服务。

开发服务器将 `/api/system/auth/...` 转发到已启动的 `http://localhost:8089/system/auth/...`，登录流程与 `AuthController` 保持一致：

- 进入登录页请求 `GET /system/auth/captcha`，展示 GIF 验证码，并按响应的 `expiresIn` 判断到期时间。到期后自动刷新并清空旧输入；切回标签页时再次检查有效期，也可点击验证码区域主动刷新。
- 提交 `POST /system/auth/login` 时仅发送 `username`、`password`、`uuid` 和 `captchaCode`。用户名为 3～32 位，密码为 8～72 位，验证码为 4 位字母或数字；登录失败后重新获取验证码。
- 使用返回的 `tokenType` 和 `accessToken` 组成 `Authorization` 请求头。勾选“保持登录”时保存到 `localStorage`，否则保存在当前标签页的 `sessionStorage`；密码不落盘，服务端控制会话有效期。
- 登录后仅获取 `GET /system/auth/userInfo`，将个人信息保存到 Pinia 用户状态中，再返回登录前的页面，默认进入 `/profile` 个人信息页。刷新页面时根据已保存的令牌重新获取用户信息；初始化失败时清理本地会话并允许重新登录。目前不请求菜单接口，也不在登录时加载动态菜单。
- 退出时请求 `POST /system/auth/logout`，成功后清理令牌、用户资料、菜单、标签页和页面缓存。会话失效返回 401 时回到登录页。

登录需要使用数据库中的有效账号及当次验证码。

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
