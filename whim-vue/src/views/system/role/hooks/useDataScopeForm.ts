import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { createDataScopeRules } from './rule'
import type { DataScopeFormProps, RoleDataScope } from './types'

export function useDataScopeForm(props: DataScopeFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RoleDataScope>({ id: props.initialModel.id, dataScope: 5, deptIds: [] })
  const { selection, loading, error, canLoad, reload } = useDeptOptions(true)
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = {
        id: model.id,
        dataScope: model.dataScope,
        deptIds: model.deptIds.map(String),
      }
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )
  watch(
    () => formModel.value.dataScope,
    (scope) => {
      if (scope !== 2) formModel.value.deptIds = []
    },
  )
  const departmentOptions = computed(() => [
    ...formModel.value.deptIds
      .filter((id) => !selection.value.names.has(String(id)))
      .map((id) => ({ key: String(id), label: `已选部门（${id}）` })),
    ...selection.value.options,
  ])
  const rules = createDataScopeRules(
    () => formModel.value.dataScope === 2,
    () => canLoad.value && !loading.value && !error.value,
  )
  return {
    formRef,
    formModel,
    departmentOptions,
    loading,
    error,
    canLoad,
    reload,
    rules: computed(() => (props.readonly ? {} : rules)),
    scopeOptions: computed(() => dictionary.getDictNumberOptions('sys_role_data_scope')),
  }
}
