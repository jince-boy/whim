---
title: 数据表格 WhimTable
description: 注册式数据表格的请求契约、查询草稿、工具栏和公开方法。
---

# 数据表格 WhimTable

`WhimTable` 将查询、加载、分页、选择、列设置和全屏整合到注册式组件中。数据和配置由 `useWhimTable` 持有，模板连接实例并提供插槽，不重复维护分页对象。

## 基本使用

以下为基于现有字典类型接口的只读列表。页面 hook：

```ts
import { useWhimTable } from '@/components/table'
import { fetchDictTypePage } from '@/views/system/dictType/hooks/api'
import type { DictTypeQuery, DictTypeResult } from '@/views/system/dictType/hooks/types'

/** 管理字典类型列表的查询、分页和刷新。 */
export function useDictTypeList() {
  const [registerTable, table] = useWhimTable<DictTypeResult, DictTypeQuery>({
    request: fetchDictTypePage,
    rowKey: (row) => row.id,
    columns: [
      { type: 'selection' },
      { title: '字典名称', key: 'name' },
      { title: '字典类型', key: 'type' },
    ],
    search: { defaultValues: { name: '', type: '' } },
    pagination: { pageSize: 10, pageSizes: [10, 20, 50] },
    scrollX: 800,
  })
  return { registerTable, model: table.searchModel, reload: table.reload }
}
```

同目录 `index.vue`：

```vue
<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useDictTypeList } from './hooks/useDictTypeList'

const { registerTable, model } = useDictTypeList()
</script>

<template>
  <WhimTable @register="registerTable">
    <template #search>
      <n-form-item-gi label="字典名称" path="name">
        <n-input v-model:value="model.name" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="字典类型" path="type">
        <n-input v-model:value="model.type" clearable />
      </n-form-item-gi>
    </template>
  </WhimTable>
</template>
```

## 请求与响应

`request` 接收 `Query & { pageNum: number; pageSize: number }`，返回 `Promise<ApiResponse<TablePage<Row> | Row[]>>`。分页响应同时具有项目外层与分页内层：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "data": [{ "id": "1", "name": "用户状态", "type": "userStatus" }],
    "total": 1,
    "currentPage": 1,
    "size": 10,
    "pages": 1
  }
}
```

不要直接返回分页对象，也不要将分页行字段写成 `records`。返回数组时使用本地分页；无 `request` 时使用 `data` 的本地数据模式。

## 配置

`useWhimTable<Row, Query>(source)` 接收普通对象、ref 或 getter。动态授权与列配置适合使用 getter。

| 配置                | 类型 / 默认                                     | 说明                                      |
| ------------------- | ----------------------------------------------- | ----------------------------------------- |
| `columns`           | `MaybeRefOrGetter<DataTableColumns<Row>>`，必填 | 原生列配置                                |
| `rowKey`            | `(row: Row) => DataTableRowKey`，必填           | 稳定主键                                  |
| `request`           | 异步请求函数                                    | 与本地模式二选一                          |
| `data`              | 数组、ref 或 getter                             | 无请求时使用                              |
| `search`            | `SearchFormOptions<Query>`                      | 创建 hook 时确定是否启用                  |
| `pagination`        | 配置对象或 `false`                              | 默认每页 10，支持 10/20/50                |
| `immediate`         | 默认启用                                        | `false` 关闭注册后的首次请求              |
| `preserveSelection` | `false`                                         | 是否保留跨页选择                          |
| `transformData`     | `(rows, query) => rows`                         | 纯展示转换，不能修改输入                  |
| `toolbar`           | `TableToolbarOptions`                           | 业务按钮与通用工具                        |
| 原生属性            | 基于 `DataTableProps`                           | 如 `scrollX`、`virtualScroll`、`rowProps` |

原生属性优先在 hook 中配置，避免与组件 attrs 重复。分页和选择由 hook 管理，不再手动绑定一套 `v-model`。

## 查询草稿与已提交条件

`table.searchModel` 是输入草稿。翻页和刷新使用上次提交的查询，不会把尚未点击搜索的输入带入请求。搜索、重置都回到第一页并清空选择。

| 方法 / 状态                       | 行为                                      |
| --------------------------------- | ----------------------------------------- |
| `reload(): Promise<boolean>`      | 按已提交条件刷新                          |
| `search(): Promise<boolean>`      | 提交草稿并查询；不执行界面表单校验        |
| `resetSearch(): Promise<boolean>` | 恢复默认值并查询                          |
| `setSearchValues(partial)`        | 只修改草稿，不查询                        |
| `getSearchValues()`               | 返回查询草稿的独立快照                    |
| `data`                            | 原始行数据，非 `transformData` 的展示结果 |
| `loading` / `error`               | 只读加载状态与错误文本                    |

界面搜索按钮先校验再提交；程序化 `search()` 与它的校验行为不同。只允许最新请求写回，失败保留上次数据。末页数据删除后，最多补查一次有效页。

## 工具栏

业务按钮通过 `toolbar.buttons` 显式启用，可选 `add`、`edit`、`delete`、`export`。工具栏还支持 `permissions`、`roles`、`showStriped`、`showColumns`、`showRefresh`、`showFullscreen`。

```ts
toolbar: {
  buttons: ['add', 'edit', 'delete', 'export'],
  permissions: {
    add: ['system:dictType:add'],
    edit: ['system:dictType:update'],
    delete: ['system:dictType:delete'],
    export: ['system:dictType:export'],
  },
}
```

权限数组内部为 OR，角色数组内部为 OR；同时配置权限与角色时，两类条件都要满足。前端支持 `*` 判断，但后端不因此跳过授权校验。

修改需要恰好选中一行；删除至少一行；导出允许无选择。加载时业务按钮禁用。刷新、列设置、斑马纹开关与全屏默认显示，斑马纹默认关闭。

边框默认来自主题设置 `tableBordered`，可由原生 `bordered`、`singleLine` 覆盖。全屏包含查询表单，支持 Esc 退出，离开页面自动退出。

## 事件与插槽

| 事件       | 参数               | 说明                                 |
| ---------- | ------------------ | ------------------------------------ |
| `register` | 实例或 `undefined` | 注册与卸载                           |
| `add`      | 无                 | 新增通知                             |
| `edit`     | 单个主键           | 修改通知                             |
| `delete`   | 主键数组快照       | 删除通知                             |
| `export`   | 主键数组快照       | 导出通知                             |
| `refresh`  | 无                 | 组件已调用刷新，处理器不要再重复请求 |

| 插槽                            | 作用域              | 用途           |
| ------------------------------- | ------------------- | -------------- |
| `search`                        | 无                  | 查询字段       |
| `searchAction`                  | `{ submit, reset }` | 替换查询按钮   |
| `toolbarBefore`                 | 无                  | 工具栏前置内容 |
| `toolbarExtra`                  | 无                  | 工具栏扩展内容 |
| `default` / `empty` / `loading` | 原生作用域          | 透传给数据表格 |

## 选择、展开与原生实例

`getCheckedRowKeys()`、`getExpandedRowKeys()` 返回独立数组；通过 `setCheckedRowKeys()`、`setExpandedRowKeys()` 更新。默认选择只保留当前展示行及树子项中存在的键。跨页选择需显式启用，并在删除后清理失效主键。

列设置按分组整组切换，新增列默认显示，`key: 'action'` 列始终显示。`transformData` 不能用于隐藏未经授权的敏感数据，授权应在接口执行。

挂载并注册后，可调用 `getTableInstance()` 获取原生 `DataTableInst`，使用 `scrollTo`、`sort`、`downloadCsv` 等能力。组件实例只公开 `reload` 和 `getTableInstance`。

## 依赖与源码

表格使用 Pinia 的主题、用户 store 与 Naive 消息环境，不能直接在没有应用上下文的 SSR 页面初始化。本页提供管理端接入代码，文档站不调用真实业务接口。

源码：`whim-vue/src/components/table`。完整业务示例：`views/system/dictType`；动态列和树列表示例：`views/system/menu`。
