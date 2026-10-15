---
title: 菜单搜索
description: 在当前用户可见且可访问的内部菜单中搜索和导航。
---

# 菜单搜索

`SearchComponent` 是管理端导航能力，读取权限 store 中的菜单并通过管理端路由跳转，不是查询业务数据的 `SearchForm`。

## 接入

```vue
<script setup lang="ts">
import { ref } from 'vue'
import SearchComponent from '@/components/search/index.vue'

const visible = ref(false)
</script>

<template>
  <n-button @click="visible = true">搜索菜单</n-button>
  <SearchComponent v-model="visible" />
</template>
```

默认 `v-model` 必填且类型为 `boolean`。没有其他公开 props、业务事件、插槽或实例方法。真实入口位于布局顶部栏。

## 搜索范围

只收集当前可见、未禁用、可访问的内部页面。目录自身不作为跳转结果，外链不会进入搜索列表。关键词按菜单名称与祖先目录名称匹配，不区分英文大小写，不支持拼音匹配。

权限菜单更新时索引重新计算。搜索结果来自当前用户的导航数据，不是授权边界；后端仍按接口权限和数据范围校验请求。

## 交互

| 操作           | 行为                               |
| -------------- | ---------------------------------- |
| 打开           | 清空上次关键词并聚焦输入框         |
| 输入关键词     | 更新匹配列表；结果变化后重置选中项 |
| ↑ / ↓          | 循环切换结果                       |
| Enter          | 打开当前选中菜单                   |
| Esc / 关闭按钮 | 关闭弹窗                           |
| 鼠标移动       | 同步当前选中项                     |

键盘事件限定在输入框，输入法组合状态与修饰组合键不触发导航。成功导航或已经位于目标页面时关闭；导航失败保留结果并提示，提交期间阻止重复导航。

## 布局与可访问性

最大宽度为 600px，并受视口宽高约束。结果区域独立滚动，选中项自动滚入可见区域；长名称允许换行。输入框与列表通过 combobox、listbox、option 和活动项关联，结果数量通过状态区域播报。

## 使用前提

依赖管理端 `permissionStore` 和 vue-router。应在权限菜单初始化后的布局中使用，不直接复制到无路由的独立页面或在 SSR 中创建管理端 store。

源码：`whim-vue/src/components/search/index.vue`、`hooks/useMenuSearch.ts`。`result.vue` 为内部展示组件。
