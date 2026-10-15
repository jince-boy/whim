# 表单与确认弹窗

统一从 `@/components/dialog` 导入，在页面 hook 的 setup 阶段调用 `useFormDialog()`。应用需要配置 Naive UI 的 ModalProvider 和 MessageProvider。

## 表单弹窗

```ts
const { openFormDialog, openDeleteDialog, openConfirmDialog } = useFormDialog()

// 表单 props 和提交模型均从 MenuForm 推导，不需要填写泛型。
const editor = openFormDialog({
  title: '修改菜单',
  formComponent: MenuForm,
  formProps: {
    initialModel: menu,
    extendedData: { parentId: menu.parentId, menuData: menus },
  },
  onSubmit: (values) => updateMenu(menu.id, values),
  onSuccess: reloadMenus,
  width: 800,
  modalProps: { positiveText: '保存', draggable: true },
})
```

表单组件用 `defineExpose({ formRef, formModel })` 暴露校验实例与编辑草稿，组件自身负责初始化和隔离草稿。`formProps` 就是该组件的原生 props，必填属性也会被检查；提交回调接收校验后的独立数据快照，后续编辑不会改变已经提交的数据。模型应使用可被 `structuredClone` 克隆的数据。

- `onSubmit` 返回项目的 `ApiResponse`：`code === 200` 时提示并关闭；否则显示错误并保留草稿。
- 自定义业务可返回 `false` 保留弹窗；返回 `true` 或 `void` 表示成功。异步工作必须返回 Promise 或使用 await。
- `successMessage` 覆盖成功提示，`false` 关闭提示。删除默认提示“删除成功”，其他弹窗默认使用接口消息或“操作成功”。
- 校验失败保留字段错误。提交异常保留草稿；项目 Axios 错误由公共拦截器提示，其他异常由弹窗提示。
- `onSuccess` 在操作成功、弹窗关闭后执行，适合刷新列表。刷新失败不会重新打开弹窗或重复保存。
- 提交期间锁定确定、取消、关闭、Esc 和遮罩；默认禁止点击遮罩关闭，空闲时可通过 `modalProps.maskClosable` 开启。
- `modalProps` 保留 Naive UI 原生配置的类型提示，内容、提交和关闭事件由封装管理。`modalProps.onAfterLeave` 可监听实际离场。
- 返回的 `editor.submit()` 走同一校验流程；`editor.close()` 在提交中返回 false；`editor.submitting` 为只读状态。
- 每个弹窗独立持有实例。页面卸载或 KeepAlive 页面停用时，只清理该 hook 创建的弹窗；已经发出的 HTTP 请求不会被撤销，晚到结果不会再操作已关闭弹窗。
- 默认宽度 600px，限制为视口宽高并支持滚动。全屏状态下弹窗挂载到当前全屏容器。

## 删除与其他确认

```ts
openDeleteDialog({
  content: `确定删除“${row.menuName}”？`,
  onSubmit: () => deleteMenu(row.id),
  onSuccess: reloadMenus,
})

openConfirmDialog({
  title: '清空确认',
  content: '确定清空操作日志？',
  modalProps: { type: 'warning', positiveText: '清空' },
  onSubmit: cleanOperLog,
  successMessage: '清理成功',
  onSuccess: reloadLogs,
})
```

确认弹窗与表单弹窗使用相同的防重复提交、错误处理和关闭约定。`content` 支持字符串或原生内容渲染函数。
