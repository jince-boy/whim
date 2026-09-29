# Whim Vue 前端开发规范

本文件适用于 `whim-vue/` 下的全部代码。AI 修改本项目时，先阅读本文件、相关页面目录和现有实现；同时遵守仓库根目录的 `AGENTS.md`。用户在当前任务中的明确要求优先。

## 项目与技术栈

- 项目是 Vue 3 + TypeScript + Vite 的单页应用，使用 Vue Router、Pinia、Naive UI、Axios 和 Sass。
- 使用 `pnpm` 管理依赖，保持 `package.json` 与 `pnpm-lock.yaml` 同步；不要混用 npm、Yarn 或其他锁文件。
- 以当前项目中实际安装的版本和配置为准。不要直接复制其他仓库的接口地址、响应字段、权限模型或旧版脚手架代码。
- 优先使用 Vue 3 Composition API、`<script setup lang="ts">` 和明确的 TypeScript 类型。

## 目录与职责

按业务功能组织代码。每个路由页面必须有自己的目录，页面入口为 `index.vue`；页面专属代码放在这个目录附近，不集中堆到全局目录。

```text
src/
├── assets/                    # 静态资源和全局样式
├── components/                # 至少被两个功能复用的组件
├── router/                    # 路由定义和导航守卫
├── stores/                    # 跨页面共享的 Pinia 状态
├── utils/
│   └── http/
│       └── index.ts           # 唯一的 Axios 实例与通用响应处理
└── views/
    └── system/
        └── user/
            ├── index.vue     # 页面结构、组件组合和事件绑定
            ├── components/   # 仅该页面使用的组件，按需创建
            └── hooks/
                ├── api.ts     # 当前页面所属业务的接口请求
                ├── useUser.ts # 页面状态、行为和异步流程
                ├── types.ts   # 页面专属类型，按需创建
                └── rule.ts    # 页面专属表单规则，按需创建
```

`hooks/` 是页面专属逻辑及其配套文件的归属目录。其中 `useXxx.ts` 是组合式函数；`api.ts`、`types.ts` 和 `rule.ts` 是配套文件。没有实际内容时不要创建空目录或空文件。

## 页面开发模型

1. 新增页面时，先确定业务域和页面目录，再创建 `index.vue`。路由指向该页面入口。
2. `index.vue` 保留模板、页面级组件组合、样式和对 hook 的调用。不要在其中直接写 Axios 请求、复杂数据转换或整段业务流程。
3. `hooks/useXxx.ts` 管理该页面的响应式状态、计算值、生命周期、表单操作、加载状态和异步流程，并向页面返回需要绑定的内容。不同职责明显且复杂时，才拆成多个 `useXxx.ts`。
4. 当前业务的接口请求函数放在 `hooks/api.ts`；接口请求与响应类型、页面表单和组件类型放在 `hooks/types.ts`；表单规则放在 `hooks/rule.ts`。
5. 页面内部可复用的视图片段放在本页的 `components/`；确认跨页面复用后，再提升到 `src/components/`。不要过早抽象。
6. `hooks/api.ts` 调用 `src/utils/http` 的统一请求方法，`useXxx.ts` 调用本页 `api.ts` 并处理页面状态。不要建立集中存放所有业务接口的大型 `src/api/` 目录；组件和 hook 不直接创建 Axios 实例。
7. 新页面应覆盖加载、成功、空数据和失败状态；提交操作要防止重复点击，失败后要恢复加载状态。

## 命名与代码风格

- 页面目录和路由静态路径使用小驼峰，例如 `views/system/userInfo/`、`/system/userInfo`；组件文件使用 PascalCase，例如 `UserForm.vue`。
- 组合式函数命名为 `useXxx`，文件名与导出名称一致。事件处理函数用 `handleXxx`，数据获取函数用 `fetchXxx` 或 `loadXxx`，布尔值用 `isXxx`、`hasXxx` 或 `showXxx`。
- Vue API 与已配置的 Naive UI API 可以使用项目维护的自动导入及生成的类型声明；业务组件、业务 hook、接口函数和其他库成员保持显式导入。
- 不使用 `any` 掩盖接口差异；对外部数据做必要的类型约束。避免无意义的包装层、重复状态和只转发参数的 helper。
- 非必要不要把一个方法拆成多个方法。简单、连续的业务逻辑应留在原方法中；只有拆出的部分具有明确职责、真实复用价值，或能明显降低复杂度时才提取。不要为了追求短方法而增加只调用一次的转发函数。
- 新增函数和方法遵守根目录注释规范，使用简洁中文说明职责；用户可见的错误信息和日志使用中文。
- 遵守现有 ESLint、Prettier 和 `.editorconfig` 设置，不单独引入另一套格式规则。

## 状态、路由与权限

- 页面局部状态放在页面 hook；只有确实需要跨页面共享的状态才放进 Pinia。
- 登录模块负责保存和清理 `token`；当前 HTTP 层从 `sessionStorage` 或 `localStorage` 读取该值。切换保存位置时清理另一处的旧值，避免请求带上过期令牌。
- 路由表集中维护在 `src/router/`，业务页面不私自注册全局守卫。登录、权限和重定向逻辑集中处理，避免在多个页面重复判断。
- 前端权限控制用于页面体验；后端仍须校验实际接口权限。不要仅凭隐藏按钮当作授权。
- 清理登录状态时同步清理相关用户状态和动态路由，避免切换账号后沿用旧权限。

## API 与后端联调

- 添加或修改接口调用前，先核对当前 Whim 后端 Controller、DTO、VO 和统一响应 `Result<T>` 的真实契约；不要以旧版前端代码猜测字段。
- Axios 实例、超时、认证头和通用错误处理集中放在 `src/utils/http/index.ts`。业务接口请求只写在对应页面的 `hooks/api.ts`，不要把页面接口堆进 HTTP 工具文件。
- 同时处理 HTTP 非 2xx 错误和响应体中的业务错误；请求层保留 401、403 等状态码并区分网络失败与超时。登录模块集中处理失效登录态，页面决定如何提示业务错误；不要在请求层直接跳转路由或弹窗。异步操作使用 `finally` 恢复加载状态。
- 开发代理与环境配置以当前后端为准；当前后端默认端口为 `8089`。生产接口地址通过环境配置管理，不在业务页面写死主机或端口。
- `VITE_` 环境变量会进入浏览器构建产物，不能放密码、密钥或其他服务端秘密。

## UI 与样式

- 全局字体使用 Inter、Noto Sans SC 和 JetBrains Mono：普通界面文字以 Inter 为首选，中文回退到 Noto Sans SC；代码及等宽内容使用 JetBrains Mono。字体族统一维护在 `src/assets/styles/fonts.scss`，Naive UI 的 `common.fontFamily` 和 `common.fontFamilyMono` 应引用同一组 CSS 变量，不要在页面或组件中分别设置全局字体。
- 应用名称、浏览器标题和底部文案统一由项目根目录的 `.env` 配置，分别使用 `VITE_APP_NAME`、`VITE_APP_TITLE` 和 `VITE_APP_FOOTER_TEXT`。底部文案中的 `{year}` 在显示时替换为当前年份。侧栏品牌名称、Logo 替代文本、页面欢迎语和浏览器标题应读取这些配置，不要在组件或路由中写死品牌文字。新增环境变量时同步更新 `env.d.ts`；环境专属配置可通过 Vite 对应模式的 `.env` 文件覆盖。
- 优先使用 Naive UI 原生组件及其属性完成页面与布局。管理端 Layout 应明确包含 Header、Sidebar、Tabbar、Main 和 Footer；导航使用 `n-menu`，页签使用 `n-tabs` 或 `n-tag`，布局使用 `n-layout` 系列组件。仅在组件属性无法满足尺寸、间距或响应式需要时补充少量局部样式，不为已有组件重新绘制外观。
- 所有 Vue 单文件组件的 `<template>` 中，组件标签统一使用 kebab-case，包括 Naive UI 组件（如 `<n-layout-sider>`、`<n-button>`）、本地组件（如 `<layout-sidebar>`）及 Vue 内置组件（如 `<router-view>`、`<transition>`、`<keep-alive>`）。开始、结束和自闭合标签保持一致；不要在模板中使用 `<NLayoutSider>`、`<LayoutSidebar>` 等 PascalCase 标签。`<script setup>` 中的组件导入名仍遵循 TypeScript 的 PascalCase 命名。
- Naive UI 模板组件通过 `unplugin-vue-components` 的 `NaiveUiResolver` 按需自动导入；Vue API 与项目已配置的 Naive UI `useXxx` API 通过 `unplugin-auto-import` 导入。业务组件、业务 hook、接口函数及未配置的库成员保持显式导入。两个插件生成的类型声明位于 `src/types/`，构建时先生成声明再执行类型检查。保留项目统一的顶层 Provider，不要全量注册组件库，也不要将消息组件挂在 `window` 上。
- 页面样式优先写在当前 `index.vue` 或本页组件的 `<style scoped lang="scss">` 中；设计变量、重置样式和确实跨页面复用的规则放在全局样式目录。
- Sass 使用 `@use` / `@forward` 管理共享样式，避免新增已弃用的 `@import`。
- 保持基本的键盘可操作性、表单标签、错误提示和常见屏幕宽度下的可用性。

## AI 交付要求

- 修改前查看目标页面、相关 hook、API、路由和共享组件，复用现有约定；不要为一个页面创建不必要的全局基础设施。
- 如用户只要求一个页面或功能，只修改实现该功能所需的文件，不顺手重构无关模块。
- 不主动编写测试代码，除非用户明确要求。完成代码修改后，运行适用的 `pnpm type-check`、定向 lint 和 `pnpm build`；现有 `pnpm lint` 带 `--fix`，不要把它当作只读检查运行。
- 遵守根目录执行安全规则；不执行 `java` 或 `maven` 命令。
- 交付时说明改了什么、如何验证、仍有什么实际限制。不要声称未运行的检查已通过。
