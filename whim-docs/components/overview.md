---
title: 组件总览
description: 六组自定义组件的入口、依赖与选用方式。
---

# 组件总览

自定义组件位于 `whim-vue/src/components`。它们围绕现有管理端场景构建，不是已经发布到 npm 的独立组件库。导入路径中的 `@` 指向管理端 `src`。

## 选择合适的入口

| 组件 / Hook                     | 解决的问题                             | 文档                                |
| ------------------------------- | -------------------------------------- | ----------------------------------- |
| `WhimTable`、`useWhimTable`     | 查询、分页、选择、列设置、工具栏、全屏 | [数据表格](/components/table)       |
| `SearchForm`、`useSearchForm`   | 独立查询草稿、校验、重置、折叠         | [查询表单](/components/search-form) |
| `useFormDialog`                 | 编辑、确认、删除和异步提交             | [弹窗](/components/dialog)          |
| `Icon`、`createIcon`、`useIcon` | Iconfont 与渲染函数                    | [图标](/components/icon)            |
| `WhimSelectIcon`                | 可搜索的单图标选择                     | [图标选择](/components/select-icon) |
| `SearchComponent`               | 登录管理端内部菜单导航                 | [菜单搜索](/components/menu-search) |
| `ThemeComponent`                | 管理端外观与布局设置                   | [主题设置](/components/theme)       |

## 公开导入

```ts
import { WhimTable, useWhimTable } from '@/components/table'
import { SearchForm, useSearchForm } from '@/components/form'
import { useFormDialog } from '@/components/dialog'
import { Icon, WhimSelectIcon, createIcon, useIcon } from '@/components/icon'
import SearchComponent from '@/components/search/index.vue'
import ThemeComponent from '@/components/theme/index.vue'
```

`search` 和 `theme` 使用 `.vue` 入口。`result.vue`、`useMenuSearch`、`useSelectIcon` 等文件属于内部实现，不作为推荐公共入口。

## 环境依赖

| 能力     | 依赖                                                      |
| -------- | --------------------------------------------------------- |
| 图标     | 项目 Iconfont CSS、字体和图标数据                         |
| 查询表单 | Naive UI；运行在浏览器时使用 `ResizeObserver`             |
| 弹窗     | `NModalProvider`、`NMessageProvider`；在组件 setup 中调用 |
| 表格     | Naive UI Provider、Pinia、主题与当前用户 store            |
| 菜单搜索 | 已初始化的权限 store、管理端 vue-router                   |
| 主题抽屉 | 主题 store、全局配置与浏览器持久化                        |

当前管理端已经配置公共运行环境。独立页面接入时，Provider 必须包住实际调用组件：

```vue
<template>
  <n-config-provider>
    <n-message-provider>
      <n-modal-provider>
        <router-view />
      </n-modal-provider>
    </n-message-provider>
  </n-config-provider>
</template>
```

原生 Naive 标签需要项目的自动组件导入配置或显式导入。弹窗封装使用 Modal Provider，不依赖 Dialog Provider。

## 共同约定

- 注册式 hook 与组件一一对应，通过 `@register` 建立联系。
- 请求、权限配置与业务事件放在页面 hook；模板负责组件组合。
- 只读 props、通知 emits 和双向 model 各有职责，不混用。
- 编辑表单维护独立草稿；查询输入修改不等于已经提交。
- 前端按钮权限控制只影响界面，后端仍必须校验功能权限与数据范围。

文档演示直接使用项目源码。需要完整管理端状态的组件提供接入代码，不在文档站执行真实业务接口。
