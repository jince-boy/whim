---
title: 图标 Icon
description: Iconfont 组件、尺寸颜色与渲染函数。
---

<script setup>
import IconDemo from '../.vitepress/theme/demos/IconDemo.vue'
</script>

# 图标 Icon

`Icon` 统一渲染项目 Iconfont。组件自行引入图标字体样式，支持纯名称和 `icon-` 前缀名称。

<DemoBlock title="图标与尺寸" description="拖动滑块调整尺寸，图标来自管理端现有 Iconfont。">
  <IconDemo />
</DemoBlock>

## 使用

```vue
<script setup lang="ts">
import { Icon, createIcon } from '@/components/icon'
</script>

<template>
  <Icon name="search" :size="20" color="#4f8466" />
  <Icon name="icon-search" size="1.25rem" />
  <n-button :render-icon="createIcon('search', 18)">搜索</n-button>
</template>
```

## 属性

| 属性    | 类型               | 默认 / 行为                                      |
| ------- | ------------------ | ------------------------------------------------ |
| `name`  | `string \| null`   | 可选；空名称不渲染；自动 trim 并移除开头 `icon-` |
| `size`  | `number \| string` | `'1em'`；数字按 px，字符串按 CSS 尺寸            |
| `color` | `string`           | 未设置时继承当前文本颜色                         |

无 model、业务事件、插槽或公开实例方法。名称必须来自项目已有字体，任意字符串不会自动生成图标。

## 渲染函数

```ts
import { createIcon, useIcon } from '@/components/icon'

const renderSearchIcon = createIcon('search', 18, '#4f8466')
const { createIcon: makeIcon } = useIcon()
```

`createIcon(name?, size?, color?)` 返回 `() => VNode`，适用于 Naive 按钮的 `renderIcon`、菜单项的 `icon` 等原生渲染入口。`useIcon()` 保留现有调用方式，返回同一个工厂函数。

## 无障碍

图标默认 `aria-hidden="true"`，承担装饰用途。只有图标的按钮，应在按钮上提供 `aria-label`；有文本的按钮由文本表达操作含义。

```vue
<n-button circle aria-label="搜索菜单">
  <template #icon><Icon name="search" /></template>
</n-button>
```

源码：`whim-vue/src/components/icon/Icon.vue`、`useIcon.ts`。字体与图标目录：`whim-vue/src/assets/iconfont`。
