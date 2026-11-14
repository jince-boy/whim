import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { roleRules } from './rule'
import type { RoleFormProps, RoleSave } from './types'

export function useRoleForm(props: RoleFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RoleSave>({ roleName: '', roleCode: '', sort: 0, status: 0, remark: null })
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = {
        id: model?.id,
        roleName: model?.roleName ?? '',
        roleCode: model?.roleCode ?? '',
        sort: model?.sort ?? 0,
        status: model?.status ?? 0,
        remark: model?.remark ?? null,
      }
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )
  return {
    formRef,
    formModel,
    rules: roleRules,
    isEdit: computed(() => !!props.initialModel),
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
  }
}
