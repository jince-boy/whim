import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { permissionRules } from './rule'
import type {
  PermissionFormProps,
  PermissionNode,
  PermissionTreeOption,
  RolePermissionSave,
} from './types'

/** 独立勾选只保存实际选择，查找节点不会改变授权集合。 */
export function usePermissionForm(props: PermissionFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<RolePermissionSave>({ id: props.role.id, permissionIds: [] })
  const pattern = ref('')
  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = { id: props.role.id, permissionIds: model.checkedKeys.map(String) }
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )
  function buildOptions(nodes: PermissionNode[]): PermissionTreeOption[] {
    return nodes.map((node) => ({
      key: String(node.id),
      label:
        node.label +
        ' · ' +
        dictionary.getDictData('sys_menu_type', node.menuType).label +
        (node.status === 0
          ? ''
          : `（${dictionary.getDictData('sys_common_status', node.status).label}）`),
      children: node.children.length ? buildOptions(node.children) : undefined,
    }))
  }
  const options = computed(() => buildOptions(props.initialModel.menus))
  const allKeys = computed(() => {
    const keys: string[] = []
    function collect(nodes: PermissionNode[]) {
      for (const node of nodes) {
        keys.push(String(node.id))
        collect(node.children)
      }
    }
    collect(props.initialModel.menus)
    return keys
  })
  const allSelected = computed(
    () => allKeys.value.length > 0 && formModel.value.permissionIds.length === allKeys.value.length,
  )
  const partlySelected = computed(
    () => formModel.value.permissionIds.length > 0 && !allSelected.value,
  )
  function selectAll(checked: boolean) {
    if (!props.readonly) formModel.value.permissionIds = checked ? [...allKeys.value] : []
  }
  return {
    formRef,
    formModel,
    pattern,
    options,
    allSelected,
    partlySelected,
    selectAll,
    rules: computed(() => (props.readonly ? {} : permissionRules)),
  }
}
