import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { roleRules } from './rule'
import type { RoleFormModel, RoleFormProps } from './types'

/** 保留无权调整的已分配角色，避免提交时静默丢失授权。 */
export function useRoleForm(props: RoleFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RoleFormModel>({ id: props.initialModel.id, roleIds: [] })
  watch(() => props.initialModel, (model) => {
    formModel.value = { id: model.id, roleIds: model.roleIds.map(String) }
    formRef.value?.restoreValidation()
  }, { immediate: true })
  const options = computed(() => props.initialModel.options.map((role) => ({
    value: String(role.id),
    label: `${role.roleName}（${role.roleCode}）${role.status === 0 ? '' : ' · ' + dictionary.getDictData('sys_common_status', role.status).label}`,
    disabled: role.disabled,
  })))
  return { formRef, formModel, rules: roleRules, options }
}

