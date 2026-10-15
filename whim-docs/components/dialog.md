---
title: 表单与确认弹窗
description: useFormDialog 的类型推导、提交锁、关闭与生命周期。
---

<script setup>
import DialogDemo from '../.vitepress/theme/demos/DialogDemo.vue'
</script>

# 表单与确认弹窗

`useFormDialog` 提供 `openFormDialog`、`openConfirmDialog`、`openDeleteDialog`。它管理校验、异步提交、关闭与页面生命周期；业务负责接口调用和成功后刷新。

<DemoBlock description="使用真实弹窗封装。操作仅修改示例内存数据，不调用后端接口。">
  <DialogDemo />
</DemoBlock>

## 环境

在组件 setup 或页面 hook 中调用，需要祖先 `NModalProvider` 与 `NMessageProvider`。这里使用 Modal Provider，不需要 Dialog Provider。

```ts
import { useFormDialog } from '@/components/dialog'

const { openFormDialog, openConfirmDialog, openDeleteDialog } = useFormDialog()
```

## 表单约定

编辑组件必须通过 `defineExpose({ formRef, formModel })` 暴露 Naive UI 表单引用与独立编辑草稿。项目示例为 `views/system/dictType/form.vue`。

```ts
const { formRef, formModel } = useDictTypeForm(props)
defineExpose({ formRef, formModel })
```

此处为已有表单入口的结构说明。`initialModel` 用于初始化草稿，不应直接修改传入数据；取消后丢弃草稿。嵌套可编辑字段也要复制。

## 打开编辑表单

在已存在的字典类型页面 hook 中：

```ts
import DictTypeForm from '@/views/system/dictType/form.vue'
import { updateDictType } from '@/views/system/dictType/hooks/api'
import { useFormDialog } from '@/components/dialog'

const { openFormDialog } = useFormDialog()

// data 来自详情接口，table 是当前页面的 useWhimTable 返回值。
openFormDialog({
  title: '修改字典类型',
  formComponent: DictTypeForm,
  formProps: { initialModel: data },
  onSubmit: updateDictType,
  onSuccess: table.reload,
})
```

示例中的 `data` 和 `table` 属于页面已有状态。封装从 `formComponent` 推导 props 与 `formModel`，无需重复声明泛型。校验通过后，`onSubmit` 收到 `structuredClone` 的独立快照。

## 通用选项

| 参数             | 说明                                              | 默认                           |
| ---------------- | ------------------------------------------------- | ------------------------------ |
| `title`          | 标题，表单弹窗必填                                | 确认：操作确认；删除：删除确认 |
| `width`          | number 按 px，也支持 CSS 字符串                   | `600px`                        |
| `modalProps`     | 原生 Modal 外观配置，排除封装控制的提交与关闭接口 | 允许拖动，遮罩不关闭           |
| `successMessage` | 自定义成功提示，`false` 关闭                      | 接口消息或操作成功             |
| `onSuccess`      | 保存成功且弹窗关闭后执行，可返回 Promise          | 无                             |

表单还需要 `formComponent`、推导后的 `formProps`、`onSubmit(model)`。确认与删除需要 `content`、`onSubmit()`；`content` 支持字符串或原生渲染函数。

## 确认与删除

```ts
openConfirmDialog({
  title: '演示确认',
  content: '确认完成当前示例操作吗？',
  successMessage: '已确认',
  onSubmit: () => true,
})
```

```ts
openDeleteDialog({
  content: '确定删除选中的记录吗？',
  onSubmit: () => deleteDictType({ dictTypeIds: ids }),
  onSuccess: table.reload,
})
```

删除示例在已有列表 hook 中使用，`ids` 为选中主键快照。`deleteDictType` 从当前功能 `hooks/api.ts` 导入。删除预设使用 warning 类型与“删除成功”提示。

## 提交结果

| `onSubmit` 返回                 | 行为                              |
| ------------------------------- | --------------------------------- |
| `ApiResponse` 且 `code === 200` | 成功关闭                          |
| `true` 或 `void`                | 成功关闭                          |
| `false`                         | 保留弹窗                          |
| 非 200 响应                     | 显示错误并保留草稿                |
| 抛出异常                        | 保留草稿；非 Axios 异常由封装提示 |

::: warning 必须返回完整 Promise
异步接口必须 `return` 或 `await`。如果先发请求却立即返回 `void`，封装会把它视为成功。Axios 错误默认由项目请求拦截器展示，封装避免重复提示。
:::

提交中禁止重复提交、取消、关闭、Esc 和遮罩关闭。提交成功后先关闭，再执行 `onSuccess`；刷新失败不会重新开放保存，避免重复写入。

## 控制器与生命周期

三个 open 方法都返回：

| 成员                         | 说明                       |
| ---------------------------- | -------------------------- |
| `submitting`                 | 只读 ref                   |
| `submit(): Promise<boolean>` | 走统一校验与提交入口       |
| `close(): boolean`           | 提交中或已关闭时返回 false |

弹窗限制视口宽高并支持滚动；处于全屏时挂载到当前全屏容器。页面卸载或 KeepAlive 停用会清理当前 hook 创建的弹窗。已经发出的网络请求不会自动取消，晚到结果不会操作已关闭的弹窗。

源码：`whim-vue/src/components/dialog/hooks/useFormDialog.ts`、`types.ts`。
