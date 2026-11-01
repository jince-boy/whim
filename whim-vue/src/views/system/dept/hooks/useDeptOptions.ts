import {
  computed,
  onMounted,
  onScopeDispose,
  ref,
  shallowRef,
  toValue,
  type MaybeRefOrGetter,
} from 'vue'
import { useUserStore } from '@/stores/modules/user'
import { fetchDeptTree } from './api'
import type { DepartmentSelection, DeptItem } from './types'

/** 部门选择和名称展示共用真实部门树，停用分支不可作为正常记录的归属。 */
export function useDeptOptions(disableInactive: MaybeRefOrGetter<boolean> = false) {
  const user = useUserStore()
  const departments = shallowRef<DeptItem[]>([])
  const loading = ref(false)
  const error = ref('')
  const canLoad = computed(
    () => user.permissionCode.includes('*') || user.permissionCode.includes('system:dept:list'),
  )
  let revision = 0

  const selection = computed<DepartmentSelection>(() => {
    const names = new Map<string, string>()
    function collect(nodes: DeptItem[], disabledAncestor = false): DepartmentSelection['options'] {
      return nodes.map((node) => {
        const disabled = disabledAncestor || node.status === 1
        names.set(String(node.id), node.deptName)
        const children = collect(node.children, disabled)
        return {
          key: String(node.id),
          label: node.deptName + (disabled ? '（停用）' : ''),
          disabled: toValue(disableInactive) && disabled,
          children: children.length ? children : undefined,
        }
      })
    }
    return { options: collect(departments.value), names }
  })

  async function reload() {
    if (!canLoad.value) return
    const version = ++revision
    loading.value = true
    error.value = ''
    try {
      const response = await fetchDeptTree()
      if (version !== revision) return
      if (response.code !== 200) throw new Error(response.message)
      departments.value = response.data
    } catch (reason) {
      if (version === revision)
        error.value = reason instanceof Error ? reason.message : '部门加载失败'
    } finally {
      if (version === revision) loading.value = false
    }
  }

  onMounted(reload)
  onScopeDispose(() => revision++)
  return { departments, selection, loading, error, canLoad, reload }
}
