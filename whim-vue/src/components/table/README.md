# 注册式列表与查询表单

统一从 `@/components/table` 导入 `WhimTable`、`useWhimTable`，从 `@/components/form` 导入 `SearchForm`、`useSearchForm`。旧版根目录 hook 和 `WhimTable2` 已移除。

## 完整列表

页面 hook 配置表格，页面模板连接实例并提供查询字段。实际接入示例见 `src/views/system/dictType/hooks/useDictType.ts` 和该功能的 `index.vue`。

```ts
import { useWhimTable } from '@/components/table'
import { fetchDictTypePage } from '@/views/system/dictType/hooks/api'
import type { DictTypeQuery, DictTypeResult } from '@/views/system/dictType/hooks/types'

/** 创建字典类型列表。 */
export function useDictTypeList() {
  const [registerTable, table] = useWhimTable<DictTypeResult, DictTypeQuery>({
    title: '字典类型',
    request: fetchDictTypePage,
    rowKey: (row) => row.id,
    columns: [
      { type: 'selection' },
      { title: '字典名称', key: 'name' },
      { title: '字典类型', key: 'type' },
    ],
    scrollX: 1000,
    search: {
      defaultValues: { name: '', type: '' },
      fields: [
        { key: 'name', label: '字典名称' },
        { key: 'type', label: '类型编码' },
      ],
    },
    pagination: { pageSize: 10, pageSizes: [10, 20, 50] },
    toolbar: { buttons: ['add', 'edit', 'delete', 'export'] },
  })
  return { registerTable, form: table.searchModel, reload: table.reload }
}
```

```vue
<WhimTable @register="registerTable" @add="openAddDialog">
  <template #search>
    <n-form-item-gi label="字典名称" path="name">
      <n-input v-model:value="form.name" clearable />
    </n-form-item-gi>
  </template>
  <template #toolbarExtra>
    <n-button @click="resetCache">刷新缓存</n-button>
  </template>
</WhimTable>
```

`WhimTableOptions<Row, Query>` 组合 Naive UI 的 `DataTableProps`。`scrollX`、`virtualScroll`、`rowProps`、排序与筛选回调等原生配置在 hook 参数中获得补全。行数据和查询字段分别由两个泛型约束。组件模板主要使用注册事件、业务事件和插槽，不再重复绑定数据、加载和分页状态。

- `request` 接收查询条件及 `pageNum`、`pageSize`，返回项目的 `ApiResponse<TablePage<Row>>`；也支持 `ApiResponse<Row[]>`。数组结果使用本地分页，树列表通常配置 `pagination: false`。
- 无 `request` 时可以传入 `data` 数组、ref 或 getter，使用本地数据模式。
- 配置可使用普通对象、ref 或 getter。动态权限和列配置使用 getter；示例见菜单页面。`search` 是否启用在创建 hook 时确定，分页的 `pageSize` 是初始值；修改页码和每页数量由表格交互管理。
- 搜索、重置及移除条件标签均回到第一页并清空选择；刷新和翻页使用已提交的条件，输入框尚未提交的修改不会影响当前结果。移除标签只撤销对应条件，保留其他未提交输入。
- 只允许最新请求写回；失败保留旧数据并显示错误；删除末页数据后最多补查一次有效页。
- `reload()` 返回 `Promise<boolean>`，便于业务操作等待刷新结果。默认注册后加载一次，`immediate: false` 可关闭自动加载。
- `search()` 提交当前草稿；界面搜索按钮会先进行表单校验。程序化调用 `search()` 不执行界面校验。
- `resetSearch()` 恢复默认查询条件并查询；`setSearchValues()` 只修改草稿，不发起请求。
- 默认只保留当前数据中的选择，包括树子节点。跨页选择需显式设置 `preserveSelection: true`，并由业务通过 `setCheckedRowKeys()` 清理已删除记录。
- 列设置记录主动隐藏的列，新增列默认显示；支持数字列 key。分组列按整组切换，`action` 列始终显示。
- `transformData(rows, query)` 仅转换表格展示，必须是纯函数；`table.data` 仍提供原始数据。菜单页面借此保留完整的父节点候选目录。

表格卡片通过 `title` 设置标题，默认“数据列表”，不显示描述。标题栏使用当前主题的浅底色，业务操作和图标工具组统一靠右；窄容器下自动换行。卡片内容区不设置内边距，表格贴合卡片边缘。表格和分页使用 Naive UI 默认尺寸，不在封装或页面中擅自设置 `small`；查询表单默认 `size: 'medium'`。只有明确要求时才覆盖尺寸。

工具栏通过 `buttons` 显式启用业务按钮。`permissions`、`roles` 使用按钮名作为键，配置后会随当前用户授权响应式更新。右侧图标工具组依次为刷新、表格设置和全屏；表格设置集中提供当前表格的斑马线开关与列显隐。通过 `showRefresh`、`showStriped`、`showColumns`、`showFullscreen` 分别关闭对应功能，同时关闭斑马线与列显隐时隐藏设置按钮。全屏包含查询表单，支持 Esc 退出，离开页面时自动退出。新增、修改、删除、导出继续使用 `@add`、`@edit`、`@delete`、`@export` 事件；修改携带单个主键，删除和导出携带选中主键快照。

工具栏和行内操作按钮统一使用 Naive UI 原生语义色：新增使用 `primary`、修改使用橙色 `warning`、删除使用红色 `error`、查看和导出统一使用蓝色 `info`。默认主题的新增按钮为绿色，行内按钮由页面列配置遵循相同约定。工具栏业务操作使用带图标的完整按钮；非新增操作采用原生 `secondary` 外观，不擅自替换为文字按钮。

默认深浅主题、按钮文字以及悬停和按下状态均使用 Naive UI 原生配色，不单独覆盖。登录页与后台共享主题配置。

表格边框统一在“主题配置 → 表格配置 → 显示表格边框”中设置，实时生效并持久化，重置主题时恢复默认关闭。工具栏不再提供边框开关；特殊表格仍可用原生 `bordered`、`singleLine` 配置覆盖主题默认值。

斑马线在每个表格右上角的“表格设置”中切换，仅影响当前表格，默认关闭。可通过原生 `striped` 配置设置初始值；`showStriped: false` 仅隐藏该表格的开关。

可用插槽：`search`、`searchAction`、`toolbarBefore`、`toolbarExtra`，以及转发到原生表格的 `default`、`empty`、`loading`。`searchAction` 接收 `submit`、`reset` 方法，可替换查询按钮。`toolbarBefore` 和 `toolbarExtra` 位于右侧业务操作区，分别在内置业务按钮之前、之后，图标工具组始终在最后。

分页、选择和展开事件由 hook 更新内部状态后，调用配置中的原生监听器。排序、筛选等其他原生事件直接交给表格；远程排序或筛选的参数转换与重新请求需要在对应监听器中按业务实现。

`getCheckedRowKeys()`、`getExpandedRowKeys()` 返回独立数组；对应的 `setCheckedRowKeys()`、`setExpandedRowKeys()` 更新状态。`getTableInstance()` 返回原生 `DataTableInst`，可以调用 `scrollTo`、`downloadCsv`、`sort` 等方法，必须在挂载并注册完成后使用。

## 独立查询表单

```ts
import { useSearchForm } from '@/components/form'

/** 创建独立的查询栏。 */
export function useUserSearch() {
  const [registerForm, form] = useSearchForm({
    defaultValues: { username: '' },
    fields: [{ key: 'username', label: '用户名' }],
  })
  return { registerForm, model: form.model, submittedValues: form.submittedValues }
}
```

```vue
<SearchForm
  @register="registerForm"
  @submit="handleSearch"
  @reset="handleSearch"
  @filter-change="handleSearch"
>
  <n-form-item-gi label="用户名" path="username">
    <n-input v-model:value="model.username" clearable />
  </n-form-item-gi>
</SearchForm>
```

`SearchForm` 使用无标题栏的紧凑独立筛选条，无需再包一层卡片。`title` 仅作为无障碍名称，默认“筛选条件”。字段标签默认置于输入区域左侧，用细分隔线区分标签和输入；搜索为带图标的主按钮，重置和“更多”使用文字按钮。操作按钮紧跟最后一个可见字段，没有整块面板收起入口。

表单配置保留原生 `FormProps`，包括 `rules`、标签位置、尺寸等；`grid` 使用原生 `GridProps`。默认根据表单内容区域的实际宽度，每格预留至少 220px、最多排列 4 列，每格宽度上限为 278px，横纵间距均为 10px。更窄的单列容器中字段随可用宽度收缩。搜索、重置及“更多”合占一格，按字段顺序自然排列和换行。默认显示一行，单列时保留第一个查询项，按钮另起一行。常用字段放在插槽前面，日期范围可使用 `:span="2"`，单列下会自动缩为一列。展开更多不清空条件、不卸载字段，也不触发查询；校验失败时自动展开全部字段。显式配置 `grid.cols` 时使用原生栅格列宽，可通过 `grid.collapsedRows` 覆盖折叠行数；`collapsed: false` 默认展示全部字段并自动换行，`grid.collapsed` 优先于该配置。字段 `path` 相对于模型，例如 `username`，不要写成 `form.username`。

`fields` 定义已应用条件标签的 `key` 和 `label`，`key` 受查询模型类型约束。可用 `format(values)` 将字典值或日期范围转换为可读文字，不改变请求参数；字典选项异步更新时标签同步更新。未配置的字段仍参与查询，但不生成标签；空字符串、空数组及空值不生成标签，`0` 和 `false` 保留。关闭标签将该字段恢复为默认值并重新查询，“清空”恢复所有默认值。

`submit`、`reset`、`filterChange` 是无参数通知事件。独立表单的处理器应读取 `submittedValues.value` 获取已提交条件；`getValues()` 获取输入草稿快照。事件发送时，校验或重置、条件快照更新已经完成。`setValues()` 修改草稿，`resetFields()` 只重置草稿和校验，`commitValues()` 将草稿提交为新快照；`submit()` 校验、提交快照后发出事件，`reset()` 重置并提交后发出事件。`validate()` 保留原生校验方法签名。

默认值和查询快照通过 `structuredClone` 隔离，支持日期、数组和嵌套数据。查询模型应使用可克隆的数据值，不放入函数、组件实例或 DOM 对象。

每次 hook 调用对应一个组件实例。卸载时解除注册；同页多个表格或表单分别调用 hook。注册缺失时实例方法立即抛出中文错误，不轮询或无限等待。查询状态只在 hook 中维护，组件不再复制一份。
