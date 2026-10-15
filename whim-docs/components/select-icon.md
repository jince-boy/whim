---
title: 图标选择 WhimSelectIcon
description: 纯图标网格选择器的模型、属性和键盘操作。
---

<script setup>
import SelectIconDemo from '../.vitepress/theme/demos/SelectIconDemo.vue'
</script>

# 图标选择 WhimSelectIcon

`WhimSelectIcon` 使用弹出面板展示八列图标网格，默认使用项目 Iconfont 数据。候选项只展示图标；选中框显示图标和对应的 key（`font_class`，例如 `search`），不附带中文名称或说明文字。

<DemoBlock>
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
| `clearable`              | `boolean`                                         | `true`                      |
| `disabled`               | `boolean`                                         | 未设置时跟随表单禁用状态    |
| `size`                   | `'small' \| 'medium' \| 'large'`                  | 跟随表单，默认 `medium`     |
| `status`                 | `'success' \| 'warning' \| 'error'`               | 跟随表单校验状态            |

打开面板后可按中文名称或类名搜索，忽略首尾空白和英文字母大小写。中文名称仅用于搜索和无障碍标签，不显示在图标旁，也不显示说明浮层。选择后关闭面板，选中框显示图标与 key；再次打开高亮当前图标并重置搜索条件。清空返回 `null`，无匹配结果时显示空状态。

面板宽度为 `488px`，窄屏时限制为视口宽度减去 `32px`。搜索框固定在顶部，图标区域限高滚动，滚动条位于面板右侧边缘，与图标保留间距。面板挂载到 `body`（浏览器元素全屏时挂载到全屏容器），不受菜单编辑等模态框的宽度或溢出裁切限制。下方“在弹窗中选择”示例使用可拖动、带滚动限制的窄弹窗展示该行为。

选中、清空触发所属 `NFormItem` 的 `input`、`change` 校验；失焦与关闭面板触发 `blur` 校验。禁用时不能打开、选择或清空。额外 HTML 属性和事件透传到触发按钮；不再透传 `NSelect` 的 `loading`、`filterable`、`multiple` 等专属属性。

## 键盘操作

聚焦选择按钮后按 Enter、空格或 ↓ 打开面板，自动聚焦搜索框。搜索框按 ↓ 进入图标网格；网格内方向键移动焦点，Home / End 到首尾，Enter / 空格选中。Esc 只关闭图标面板并回到选择按钮；面板打开时 Tab 在搜索框和图标按钮间循环，不会被外层模态框抢走焦点。

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
