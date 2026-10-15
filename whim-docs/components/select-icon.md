---
title: 图标选择 WhimSelectIcon
description: 可搜索图标单选器的模型、属性和原生透传。
---

<script setup>
import SelectIconDemo from '../.vitepress/theme/demos/SelectIconDemo.vue'
</script>

# 图标选择 WhimSelectIcon

`WhimSelectIcon` 基于 Naive UI `NSelect`，统一图标预览、关键词过滤与单选值。默认使用项目 Iconfont 数据。

<DemoBlock description="搜索图标中文名称或类名，选择后同步预览；清空返回 null。">
  <SelectIconDemo />
</DemoBlock>

## 使用

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { Icon, WhimSelectIcon } from '@/components/icon'

const icon = ref<string | null>('search')
</script>

<template>
  <WhimSelectIcon v-model="icon" />
  <Icon :name="icon" :size="24" />
</template>
```

默认 `v-model` 必填，类型为 `string | null`。值是 `font_class`，例如 `search`，不是中文标签或渲染函数。

## 属性

| 属性                     | 类型                                              | 默认                        |
| ------------------------ | ------------------------------------------------- | --------------------------- |
| `iconData`               | `readonly { name: string; font_class: string }[]` | `iconfont.json` 的 `glyphs` |
| `placeholder`            | `string`                                          | 请选择图标                  |
| `searchInputPlaceholder` | `string`                                          | 搜索图标名称或类名          |

选项文本为“名称 (font_class)”。过滤和键盘交互使用原生 Select，默认可清空，固定单选。`size`、`disabled`、`loading`、表单状态等其他属性透传到 `NSelect`；组件声明的 `multiple: false`、过滤和渲染规则由封装管理。

## 自定义候选范围

```vue
<WhimSelectIcon
  v-model="icon"
  :icon-data="[
    { name: '搜索', font_class: 'search' },
    { name: '向上', font_class: 'arrow-up' },
  ]"
/>
```

自定义数据仅限制候选范围，仍需存在对应图标字体。组件不加载远程图标，也不把图标名称解释为 HTML。

没有额外业务 emits、插槽和公开实例方法。源码：`whim-vue/src/components/icon/WhimSelectIcon.vue`，内部 `useSelectIcon` 不作为公共导出。
