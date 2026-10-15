---
title: 主题设置
description: 管理端主题抽屉、默认配置和持久化规则。
---

# 主题设置

`ThemeComponent` 提供管理端主题配置抽屉。它通过主题 store 的 actions 修改状态，并将配置保存到浏览器 `localStorage['themeConfig']`。

## 接入

```vue
<script setup lang="ts">
import { ref } from 'vue'
import ThemeComponent from '@/components/theme/index.vue'

const visible = ref(false)
</script>

<template>
  <n-button @click="visible = true">主题设置</n-button>
  <ThemeComponent v-model="visible" />
</template>
```

默认 model 为必填 `boolean`，抽屉宽度 360。无额外公开 props、业务事件、插槽或实例方法。

## 配置能力

| 分类   | 内容                                     |
| ------ | ---------------------------------------- |
| 布局   | 侧栏、混合、顶部布局                     |
| 主题   | 浅色、深色、跟随系统、基础主色、菜单反色 |
| 页面   | 页面切换动画、Logo 显示                  |
| 标签页 | 显示、缓存、图标、标签风格               |
| 水印   | 显示与文字                               |
| 表格   | 是否显示表格边框                         |
| 恢复   | 重置为默认配置                           |

标签风格为 `button`（按钮风格）、`line`（下划线分割）、`browser`（浏览器标签）。配置缓存页不代表所有页面自动缓存，仍需页面菜单自身允许。

## 默认值

| 字段                     | 默认      |
| ------------------------ | --------- |
| `theme`                  | `light`   |
| `layoutMode`             | `side`    |
| `pageTransition`         | `slide`   |
| `primaryColor`           | `#4f8466` |
| `menuInverted`           | `false`   |
| `showLogo`               | `true`    |
| `showTabs` / `cacheTabs` | `true`    |
| `showTabIcon`            | `false`   |
| `tabStyle`               | `browser` |
| `showWatermark`          | `false`   |
| `tableBordered`          | `false`   |

## 色彩和持久化

基础色在 `config/NaiveTheme.ts`，主题 store 从基础色派生 hover、pressed 等颜色。深色先提亮基础色，再派生交互色。浅色页面背景为 `#f6f8f4`。

主题配置加载时校验缓存，并补齐新增字段；缓存不可用时仍允许当前页面修改设置。旧默认绿色 `#18a058` 会迁移到当前默认色，其他有效自定义颜色保留。

## 与文档站的关系

本站复用基础色、字体与 Naive 覆盖配置，但有独立的文档布局和明暗状态。管理端抽屉不会修改文档导航，也不会将管理端布局状态写到文档站。主题 store 初始化使用浏览器 API，不应在服务器渲染阶段创建。

源码：`whim-vue/src/components/theme/index.vue`、`stores/modules/theme.ts`、`config/NaiveTheme.ts`、`config/TabStyle.ts`。
