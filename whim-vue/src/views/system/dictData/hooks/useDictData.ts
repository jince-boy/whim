import {
  deleteDictData,
  exportDictData,
  fetchDictDataDetail,
  fetchDictDataPage,
  insertDictData,
  updateDictData,
} from './api'
import { type DataTableColumn, NButton, NSpace, NTag, useMessage } from 'naive-ui'
import type { DictData } from '@/views/system/dictData/hooks/types.ts'
import { useFormDialog } from '@/components/dialog/useFormDialog.ts'
import editForm from '@/views/system/dictData/form.vue'

export interface DictDataProps {
  type: string
}

export function useDictData(props: DictDataProps) {
  const router = useRouter()
  const message = useMessage()
  const { openFormDialog, openDeleteDialog } = useFormDialog()

  const form = reactive({
    label: '',
  })

  const pagination = reactive({
    page: 1,
    pageSize: 10,
    itemCount: 0,
    pageCount: 0,
  })

  const tableColumns: DataTableColumn[] = [
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

  const tableData = ref<object[]>([])

  const loading = ref(false)

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
    openFormDialog<DictData>({
      title: '新建字典数据',
      model: {
        id: '',
        dictType: props.type,
        label: '',
        value: '',
        listClass: 'default',
        sort: 0,
        remark: '',
      },
      formComponent: editForm,
      onSubmit: async (formModel, dialog) => {
        return insertDictData(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getDictDataPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }

  /**
   * 打开修改字典数据对话框
   * @param id
   */
  const openEditDialog = async (id: string | number) => {
    const data = await getDictDataDetail(id)
    openFormDialog<DictData>({
      title: '修改字典',
      formComponent: editForm,
      model: data,
      onSubmit: async (formModel, dialog) => {
        return updateDictData(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getDictDataPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }

  /**
   * 删除字典类型
   * @param row
   */
  const removeDictData = (row: DictData) => {
    openDeleteDialog({
      content: `确定要删除${row.label}吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteDictData({ dictDataIds: [row.id] }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getDictDataPage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }
  /**
   * 批量删除字典类型
   */
  const removeDictDataBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个字典类型吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteDictData({ dictDataIds: ids }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getDictDataPage()
          } else {
            message.error(res.message)
          }
        })
      },
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

  /**
   * 获取字典数据分页
   */
  const getDictDataPage = () => {
    loading.value = true
    fetchDictDataPage({
      ...form,
      dictType: props.type,
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
    }).then((res) => {
      tableData.value = res.data.data
      pagination.page = res.data.currentPage
      pagination.pageSize = res.data.size
      pagination.itemCount = res.data.total
      pagination.pageCount = res.data.pages
      loading.value = false
    })
  }

  const backDictTypePage = () => {
    router.back()
  }
  /** 接收搜索表单的重置事件，由页面恢复查询条件。 */
  const handleReset = (values: Record<string, unknown>) => {
    Object.assign(form, values)
  }

  onMounted(() => {
    getDictDataPage()
  })

  return {
    form,
    handleReset,
    pagination,
    tableColumns,
    tableData,
    loading,
    getDictDataPage,
    openAddDialog,
    openEditDialog,
    removeDictDataBatch,
    backDictTypePage,
    exportExcel,
  }
}
