import { computed, onScopeDispose, ref, shallowRef, toRaw, useTemplateRef, watch } from 'vue'
import type { Ref } from 'vue'
import { isAxiosError } from 'axios'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'
import { message } from '@/plugins'
import {
  assignRoleDataScope,
  assignRolePermissions,
  fetchRoleDataScope,
  fetchRolePermissions,
} from './api'
import type {
  RoleAuthorizationTab,
  RoleContextProps,
  RoleDataScope,
  RolePermissions,
} from './types'
import type PermissionForm from '../PermissionForm.vue'
import type DataScopeForm from '../DataScopeForm.vue'

/** 在同一弹窗中维护两个独立授权草稿，仅保存当前页签对应的授权。 */
export function useRoleAuthorization(
  props: RoleContextProps,
  show: Ref<boolean>,
  emit: (event: 'saved') => void,
) {
  const user = useUserStore()
  const auth = useAuthStore()
  const permissionsForm = useTemplateRef<InstanceType<typeof PermissionForm>>('permissionsForm')
  const dataScopeForm = useTemplateRef<InstanceType<typeof DataScopeForm>>('dataScopeForm')
  const permissions = shallowRef<RolePermissions>()
  const dataScope = shallowRef<RoleDataScope>()
  const permissionError = ref('')
  const scopeError = ref('')
  const loading = ref(false)
  const saving = ref(false)
  const activeTab = ref<RoleAuthorizationTab>('permissions')
  const canPermissions = computed(() => user.hasPermission('system:rolePermission:list'))
  const canScope = computed(() => user.hasPermission('system:role:dataScope:list'))
  const permissionReadonly = computed(() => !user.hasPermission('system:rolePermission:assign'))
  const scopeReadonly = computed(() => !user.hasPermission('system:role:dataScope:assign'))
  const canSave = computed(() =>
    activeTab.value === 'permissions' ? !permissionReadonly.value : !scopeReadonly.value,
  )
  const currentReady = computed(() =>
    activeTab.value === 'permissions' ? !!permissions.value : !!dataScope.value,
  )
  let requestVersion = 0

  /** 两个页签分别保留加载错误，无权查询的页签不发起请求。 */
  async function reload() {
    const version = ++requestVersion
    const token = auth.getToken
    loading.value = true
    permissions.value = undefined
    dataScope.value = undefined
    permissionError.value = ''
    scopeError.value = ''
    const results = await Promise.allSettled([
      canPermissions.value ? fetchRolePermissions(props.role.id) : Promise.resolve(undefined),
      canScope.value ? fetchRoleDataScope(props.role.id) : Promise.resolve(undefined),
    ])
    if (version !== requestVersion || token !== auth.getToken || !show.value) return
    const [permissionResult, scopeResult] = results
    if (permissionResult.status === 'fulfilled' && permissionResult.value?.code === 200) {
      permissions.value = permissionResult.value.data
    } else if (canPermissions.value) {
      permissionError.value = '功能权限加载失败，请重新加载'
    }
    if (scopeResult.status === 'fulfilled' && scopeResult.value?.code === 200) {
      dataScope.value = scopeResult.value.data
    } else if (canScope.value) {
      scopeError.value = '数据范围加载失败，请重新加载'
    }
    loading.value = false
  }

  /** 验证并保存当前页签，另一页签尚未提交的草稿保持不变。 */
  async function save() {
    if (saving.value || loading.value || !canSave.value || !currentReady.value) return
    const version = requestVersion
    const token = auth.getToken
    saving.value = true
    try {
      let response
      if (activeTab.value === 'permissions') {
        const form = permissionsForm.value
        if (!form?.formRef) return
        await form.formRef.validate()
        if (version !== requestVersion || token !== auth.getToken || !show.value) return
        response = await assignRolePermissions(structuredClone(toRaw(form.formModel)))
      } else {
        const form = dataScopeForm.value
        if (!form?.formRef) return
        await form.formRef.validate()
        if (version !== requestVersion || token !== auth.getToken || !show.value) return
        response = await assignRoleDataScope(structuredClone(toRaw(form.formModel)))
      }
      if (version !== requestVersion || token !== auth.getToken || !show.value) return
      if (response.code !== 200) {
        message.error(response.message)
        return
      }
      message.success(response.message)
      emit('saved')
    } catch (error) {
      if (!Array.isArray(error) && !isAxiosError(error))
        message.error(error instanceof Error ? error.message : '授权保存失败')
    } finally {
      saving.value = false
    }
  }

  watch(
    [show, () => props.role.id],
    ([visible]) => {
      if (visible) {
        activeTab.value = canPermissions.value ? 'permissions' : 'dataScope'
        void reload()
      } else {
        requestVersion++
        loading.value = false
      }
    },
    { immediate: true },
  )
  onScopeDispose(() => requestVersion++)
  return {
    activeTab,
    permissions,
    dataScope,
    permissionError,
    scopeError,
    loading,
    saving,
    canPermissions,
    canScope,
    permissionReadonly,
    scopeReadonly,
    canSave,
    currentReady,
    reload,
    save,
  }
}
