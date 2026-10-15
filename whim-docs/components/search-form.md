---
title: 查询表单 SearchForm
description: 查询草稿、注册、字段校验、响应式布局与提交快照。
---

<script setup>
import SearchFormDemo from '../.vitepress/theme/demos/SearchFormDemo.vue'
</script>

# 查询表单 SearchForm

独立查询栏使用 `SearchForm` 与 `useSearchForm`。hook 维护模型，组件负责原生表单、网格、搜索和重置按钮。

<DemoBlock description="输入内容后点击搜索，观察已提交快照；重置恢复默认值并通知查询。">
  <SearchFormDemo />
</DemoBlock>

## 基本使用

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { SearchForm, useSearchForm } from '@/components/form'

const [registerForm, form] = useSearchForm({ defaultValues: { username: '' } })
const model = form.model
const submitted = ref(form.getValues())

/** 读取校验或重置后的查询快照。 */
function handleSearch() {
  submitted.value = form.getValues()
}
</script>

<template>
  <SearchForm @register="registerForm" @submit="handleSearch" @reset="handleSearch">
    <n-form-item-gi label="用户名" path="username">
      <n-input v-model:value="model.username" clearable />
    </n-form-item-gi>
  </SearchForm>
  <pre>{{ submitted }}</pre>
</template>
```

业务页面中将状态与处理器放入页面 hook，模板保留注册与字段。字段 `path` 相对于模型，使用 `username`，不要写 `model.username`。

## 配置

`useSearchForm<Values>(source)` 支持对象、ref 和 getter；参数基于 `Omit<FormProps, 'model'>`：

| 参数            | 类型 / 默认      | 说明                 |
| --------------- | ---------------- | -------------------- |
| `defaultValues` | `Values`，必填   | 初始值与重置来源     |
| `grid`          | 原生 `GridProps` | 列数、间距与折叠配置 |
| `searchText`    | `搜索`           | 提交按钮文本         |
| `resetText`     | `重置`           | 重置按钮文本         |
| `rules` 等      | 原生 `FormProps` | 校验、标签、尺寸等   |

默认 `labelWidth: 'auto'`、`labelPlacement: 'left'`，有 rules 时默认显示字段反馈；网格 `xGap`、`yGap` 均为 16。

## 布局与折叠

组件按实际容器宽度布局：小于 640px 为 1 列，640px 起 2 列，960px 起 3 列，1280px 起 4 列。操作按钮合占一格，紧跟查询字段排列。

默认折叠一行；单列时显示一个查询项和单独一行操作按钮。展开不会丢弃已填条件。校验失败自动展开，便于定位错误。可通过 `grid.cols`、`grid.collapsedRows`、`grid.collapsed: false` 覆盖默认行为。

## Hook 返回值

返回 `[registerForm, form]`：

| 成员                         | 说明                                  |
| ---------------------------- | ------------------------------------- |
| `model`                      | 可绑定的查询草稿 ref                  |
| `getValues()`                | 返回独立快照                          |
| `setValues(partial)`         | 合并字段并克隆隔离                    |
| `resetFields()`              | 恢复默认值与清理校验，不发送通知      |
| `validate`                   | 保留原生 `FormInst['validate']` 签名  |
| `submit(): Promise<boolean>` | 校验成功后发出 submit；无效返回 false |
| `reset()`                    | 重置后发出 reset                      |

默认值、草稿与快照通过 `structuredClone` 隔离。数据可包含日期、数组和嵌套对象，但不要放函数、组件实例或 DOM 对象。

## 组件契约

事件 `register(instance | undefined)`、`submit()`、`reset()`。提交和重置事件不携带模型；处理器通过 `getValues()` 读取快照。重置通知发出时，默认值已经恢复。

默认插槽提供 `n-form-item-gi` 字段；`action` 插槽接收 `{ submit, reset }`，可替换操作区。组件实例公开 `submit`、`reset`、`validate`、`restoreValidation`。其他属性透传给内部 `n-form`，hook 配置优先。

一个 hook 只能连接一个实例。命令式实例方法应在挂载注册后调用；注册缺失会立即抛出中文错误。

源码：`whim-vue/src/components/form`。
