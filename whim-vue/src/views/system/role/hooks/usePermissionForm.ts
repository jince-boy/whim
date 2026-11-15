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
  const tree = computed(() => {
    const keys: string[] = []
    // 同一次遍历生成选项与全选主键，避免为全选再次递归整棵菜单树。
    function buildOptions(nodes: PermissionNode[]): PermissionTreeOption[] {
      return nodes.map((node) => {
        const key = String(node.id)
        keys.push(key)
        return {
          key,
          label:
            node.label +
            ' · ' +
            dictionary.getDictData('sys_menu_type', node.menuType).label +
            (node.status === 0
              ? ''
              : `（${dictionary.getDictData('sys_common_status', node.status).label}）`),
          children: node.children.length ? buildOptions(node.children) : undefined,
        }
      })
    }
    return { options: buildOptions(props.initialModel.menus), keys }
  })
  const allSelected = computed(
    () =>
      tree.value.keys.length > 0 && formModel.value.permissionIds.length === tree.value.keys.length,
  )
  const partlySelected = computed(
    () => formModel.value.permissionIds.length > 0 && !allSelected.value,
  )
  function selectAll(checked: boolean) {
    if (!props.readonly) formModel.value.permissionIds = checked ? [...tree.value.keys] : []
  }
  return {
    formRef,
    formModel,
    pattern,
    options: computed(() => tree.value.options),
    allSelected,
    partlySelected,
    selectAll,
    rules: computed(() => (props.readonly ? {} : permissionRules)),
  }
}
