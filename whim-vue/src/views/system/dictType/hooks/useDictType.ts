import { type DataTableColumn, NButton, NSpace, useMessage } from 'naive-ui'
import {
  deleteDictType,
  exportDictType,
  fetchDictTypeDetail,
  fetchDictTypePage,
  insertDictType,
  resetDictCache,
  updateDictType,
} from './api'
import type { DictType } from '@/views/system/dictType/hooks/types.ts'
import editForm from '@/views/system/dictType/form.vue'
import { useFormDialog } from '@/components/dialog/useFormDialog.ts'
export function useDictType() {
  const router = useRouter()
  const message = useMessage()
  const { openFormDialog, openDeleteDialog } = useFormDialog()

  const form = reactive({
    name: '',
    type: '',
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
      title: '字典名称',
      key: 'name',
      align: 'center',
    },
    {
      title: '字典类型',
      key: 'type',
      align: 'center',
      render(row) {
        return h(
          NButton,
          {
            text: true,
            type: 'primary',
            onClick: () => {
              router.push(`/system/dictData/${row.type}`)
            },
          },
          { default: () => row.type },
        )
      },
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
        const dictType = row as DictType
        return h(NSpace, { justify: 'center' }, () => [
          h(
            NButton,
            {
              text: true,
              type: 'info',
              onClick: async () => {
                await openEditDialog(dictType.id)
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
                removeDictType(dictType)
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
  const getDictTypeDetail = async (id: number | string): Promise<DictType> => {
    const res = await fetchDictTypeDetail({ id })
    return res.data as DictType
  }

  /**
   * 打开新增字典对话框
   */
  const openAddDialog = () => {
    openFormDialog<DictType>({
      title: '新建字典',
      formComponent: editForm,
      onSubmit: async (formModel, dialog) => {
        return insertDictType(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getDictTypePage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }

  /**
   * 打开修改字典对话框
   * @param id
   */
  const openEditDialog = async (id: string | number) => {
    const data = await getDictTypeDetail(id)
    openFormDialog<DictType>({
      title: '修改字典',
      formComponent: editForm,
      model: data,
      onSubmit: async (formModel, dialog) => {
        return updateDictType(formModel).then((res) => {
          if (res.code == 200) {
            dialog.destroy()
            message.success(`${dialog.title}成功`)
            getDictTypePage()
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
  const removeDictType = (row: DictType) => {
    openDeleteDialog({
      content: `确定要删除${row.name}吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteDictType({ dictTypeIds: [row.id] }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getDictTypePage()
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
  const removeDictTypeBatch = (ids: Array<string | number>) => {
    openDeleteDialog({
      content: `确定要删除选中的 ${ids.length} 个字典类型吗？删除后无法恢复！`,
      onSubmit: () => {
        return deleteDictType({ dictTypeIds: ids }).then((res) => {
          if (res.code == 200) {
            message.success(`删除成功`)
            getDictTypePage()
          } else {
            message.error(res.message)
          }
        })
      },
    })
  }

  /**
   * 导出字典类型
   */
  const exportExcel = async () => {
    await exportDictType()
  }

  /**
   * 重置字典缓存
   */
  const handleResetDictCache = () => {
    resetDictCache().then((res) => {
      if (res.code == 200) {
        message.success(`重置成功`)
      }
    })
  }
  /**
   * 获取字典类型分页数据
   */
  const getDictTypePage = () => {
    console.log(pagination)
    loading.value = true
    fetchDictTypePage({
      ...form,
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

  /** 接收搜索表单的重置事件，由页面恢复查询条件。 */
  const handleReset = (values: Record<string, unknown>) => {
    Object.assign(form, values)
  }

  onMounted(() => {
    getDictTypePage()
  })
  return {
    form,
    handleReset,
    tableColumns,
    tableData,
    pagination,
    loading,
    getDictTypePage,
    openAddDialog,
    openEditDialog,
    removeDictTypeBatch,
    handleResetDictCache,
    exportExcel,
  }
}
