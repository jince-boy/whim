import { useWhimTable } from '@/components/table'
import {
  deleteDictData,
  exportDictData,
  fetchDictDataDetail,
  fetchDictDataPage,
  insertDictData,
  updateDictData,
} from './api'
import { type DataTableColumn, NButton, NSpace, NTag } from 'naive-ui'
import type {
  DictData,
  DictDataQuery,
  DictDataResult,
} from '@/views/system/dictData/hooks/types.ts'
import { useFormDialog } from '@/components/dialog'
import editForm from '@/views/system/dictData/form.vue'

export interface DictDataProps {
  type: string
}

/** 配置注册式列表并处理字典数据业务操作。 */
export function useDictData(props: DictDataProps) {
  const router = useRouter()
  const { openFormDialog, openDeleteDialog } = useFormDialog()

  const tableColumns: DataTableColumn<DictDataResult>[] = [
    {
      type: 'selection',
    },
    {
      title: '字典标签',
      key: 'label',
      align: 'center',
      render(row: object) {
        const dictData = row as DictData
        return h(
          NTag,
          {
            bordered: false,
            type: dictData.listClass,
          },
          { default: () => dictData.label },
        )
      },
    },
    {
      title: '字典键值',
      key: 'value',
      align: 'center',
    },
    {
      title: '排序',
      key: 'sort',
      align: 'center',
    },
    {
      title: '备注',
      key: 'remark',
      align: 'center',
    },
    {
      title: '创建时间',
      key: 'createTime',
      align: 'center',
    },
    {
      title: '操作',
      key: 'action',
      align: 'center',
      render(row: object) {
        const dictData = row as DictData
        return h(NSpace, { justify: 'center' }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'info',
              onClick: async () => {
                await openEditDialog(dictData.id)
              },
            },
            { default: () => '修改' },
          ),
          h(
            NButton,
            {
              text: true,
              type: 'error',
              onClick: () => {
                removeDictData(dictData)
              },
            },
            { default: () => '删除' },
          ),
        ])
      },
    },
  ]

  const [registerTable, { searchModel: form, reload: getDictDataPage, resetSearch }] = useWhimTable<
    DictDataResult,
    DictDataQuery
  >({
    columns: tableColumns,
    rowKey: (row) => row.id,
    request: (query) => fetchDictDataPage({ ...query, dictType: props.type }),
    search: { defaultValues: { label: '' } },
    toolbar: {
      buttons: ['add', 'edit', 'delete', 'export'],
      permissions: {
        add: ['system:dictData:add'],
        edit: ['system:dictData:edit'],
        delete: ['system:dictData:delete'],
        export: ['system:dictData:export'],
      },
    },
  })

  /**
   * 获取字典类型详情
   * @param id
   */
  const getDictDataDetail = async (id: number | string): Promise<DictData> => {
    const res = await fetchDictDataDetail({ id })
    return res.data as DictData
  }

  /**
   * 打开新增字典数据对话框
   */
  const openAddDialog = () => {
    openFormDialog({
      title: '新建字典数据',
      formProps: {
        initialModel: {
          id: '',
          dictType: props.type,
          label: '',
          value: '',
          listClass: 'default',
          sort: 0,
          remark: '',
        },
      },
      formComponent: editForm,
      onSubmit: insertDictData,
      successMessage: '新增字典数据成功',
      onSuccess: getDictDataPage,
    })
  }

  /**
   * 打开修改字典数据对话框
   * @param id
   */
  const openEditDialog = async (id: string | number) => {
    const data = await getDictDataDetail(id)
    openFormDialog({
      title: '修改字典数据',
      formComponent: editForm,
      formProps: { initialModel: data },
      onSubmit: updateDictData,
      successMessage: '修改字典数据成功',
      onSuccess: getDictDataPage,
    })
  }

  /**
   * 删除字典类型
   * @param row
   */
  const removeDictData = (row: DictData) => {
    openDeleteDialog({
      content: `确定要删除${row.label}吗？删除后无法恢复！`,
      onSubmit: () => deleteDictData({ dictDataIds: [row.id] }),
      onSuccess: getDictDataPage,
    })
  }
  /**
   * 批量删除字典类型
   */
  const removeDictDataBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个字典类型吗？删除后无法恢复！`,
      onSubmit: () => deleteDictData({ dictDataIds: ids }),
      onSuccess: getDictDataPage,
    })
  }

  /**
   * 导出字典数据
   */
  const exportExcel = async () => {
    await exportDictData({
      dictType: props.type,
    })
  }

  /** 返回字典类型列表。 */
  const backDictTypePage = () => {
    router.back()
  }

  // 路由类型变化时清空查询条件；首次加载交给表格注册流程。
  watch(
    () => props.type,
    () => {
      void resetSearch()
    },
  )

  return {
    registerTable,
    form,
    getDictDataPage,
    openAddDialog,
    openEditDialog,
    removeDictDataBatch,
    backDictTypePage,
    exportExcel,
  }
}
