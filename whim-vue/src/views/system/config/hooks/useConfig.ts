import { computed, h } from 'vue'
import { NCode, NSpace, type DataTableColumns } from 'naive-ui'
import { TableAction, useWhimTable } from '@/components/table'
import type { SearchFormField } from '@/components/form'
import { useFormDialog } from '@/components/dialog'
import { useUserStore } from '@/stores/modules/user'
import ConfigForm from '../form.vue'
import ConfigImage from '../components/ConfigImage.vue'
import ConfigValueTypeTag from '../components/ConfigValueTypeTag.vue'
import { addConfig, deleteConfig, fetchConfigPage, updateConfig, uploadConfigImage } from './api'
import { getEffectiveConfigValue, getSiteSettingField, refreshSiteSettings } from './useSiteSettings'
import type { ConfigItem, ConfigQuery } from './types'

const searchFields: (SearchFormField<ConfigQuery> & { maxLength: number })[] = [
  { key: 'configName', label: '配置名称', maxLength: 128 },
  { key: 'configKey', label: '配置键', maxLength: 128 },
  { key: 'remark', label: '备注', maxLength: 255 },
]

/** 两个分类各自维护分页与查询条件，写入完成后仅刷新所属列表。 */
export function useConfig(builtIn: boolean) {
  const user = useUserStore()
  const { openFormDialog, openDeleteDialog } = useFormDialog()
  const canUpdate = computed(() => user.hasPermission('system:config:update'))
  const canDelete = computed(() => !builtIn && user.hasPermission('system:config:delete'))
  const columns = computed<DataTableColumns<ConfigItem>>(() => {
    const items: DataTableColumns<ConfigItem> = [
      { title: '配置名称', key: 'configName', minWidth: 135, ellipsis: { tooltip: true } },
      { title: '配置键', key: 'configKey', minWidth: 190, ellipsis: { tooltip: true } },
      {
        title: '值类型', key: 'valueType', minWidth: 88,
        render: (row) => h(ConfigValueTypeTag, { value: row.valueType }),
      },
      {
        title: '配置值', key: 'configValue', minWidth: 130, ellipsis: { tooltip: true },
        render: (row) => {
          const value = getEffectiveConfigValue(row)
          if (row.valueType === 'IMAGE') {
            return h(ConfigImage, { configId: row.id, fileId: value, siteLogo: getSiteSettingField(row) === 'logoFileId' })
          }
          if (row.valueType === 'JSON') return h(NCode, { language: 'json', code: value, inline: true, trim: false })
          if (row.valueType === 'RADIO') {
            return row.valueOptions?.find(option => option.value === value)?.label ?? value
          }
          return value || '—'
        },
      },
      { title: '备注', key: 'remark', minWidth: 100, ellipsis: { tooltip: true } },
    ]
    if (canUpdate.value || canDelete.value) {
      items.push({
        title: '操作', key: 'action', fixed: 'right', width: 90,
        render: (row) => h(NSpace, { size: 6, wrap: false }, () => [
          canUpdate.value && h(TableAction, { label: '修改', icon: 'pencil', type: 'warning', onClick: () => openEditDialog(row) }),
          canDelete.value && !row.builtIn && h(TableAction, { label: '删除', icon: 'trash-2', type: 'error', onClick: () => removeConfig(row) }),
        ]),
      })
    }
    return items
  })
  const [registerTable, table] = useWhimTable<ConfigItem, ConfigQuery>({
    title: builtIn ? '系统配置' : '自定义配置',
    columns,
    rowKey: (row) => String(row.id),
    scrollX: 838,
    request: (query) => fetchConfigPage({ ...query, builtIn }),
    search: {
      defaultValues: { configName: '', configKey: '', remark: '' },
      fields: searchFields,
    },
    toolbar: { buttons: builtIn ? [] : ['add'], permissions: { add: ['system:config:create'] } },
  })

  function openAddDialog() {
    openFormDialog({
      title: '新增自定义配置', formComponent: ConfigForm,
      onSubmit: async ({ imageFile, ...data }) => {
        if (imageFile) data.configValue = await uploadConfigImage(imageFile)
        return addConfig(data)
      },
      onSuccess: table.reload,
    })
  }

  function openEditDialog(row: ConfigItem) {
    const siteField = getSiteSettingField(row)
    openFormDialog({
      title: row.builtIn ? '修改系统配置' : '修改自定义配置',
      formComponent: ConfigForm, formProps: { initialModel: row },
      onSubmit: async (model) => {
        // 原先继承部署设置时，直接保存不能把当前部署值固化成数据库覆盖值。
        const inherited = siteField && !row.configValue?.trim()
        const configValue = model.imageFile ? await uploadConfigImage(model.imageFile) : model.configValue
        return updateConfig({
          id: row.id,
          configValue: inherited && configValue === getEffectiveConfigValue(row) ? '' : configValue,
          ...(row.builtIn ? {} : {
            configName: model.configName, remark: model.remark, valueType: model.valueType, valueOptions: model.valueOptions,
          }),
        })
      },
      onSuccess: siteField
        ? () => Promise.all([table.reload(), refreshSiteSettings()])
        : table.reload,
    })
  }

  function removeConfig(row: ConfigItem) {
    openDeleteDialog({
      content: `确定删除自定义配置「${row.configName}」（${row.configKey}）吗？`,
      onSubmit: () => deleteConfig(row.id), onSuccess: table.reload,
    })
  }

  return { registerTable, form: table.searchModel, searchFields, openAddDialog }
}
