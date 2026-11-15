<script setup lang="ts">
import PermissionForm from '../PermissionForm.vue'
import DataScopeForm from '../DataScopeForm.vue'
import type { RoleContextProps } from '../hooks/types'
import { useRoleAuthorization } from '../hooks/useRoleAuthorization'

const props = defineProps<RoleContextProps>()
const show = defineModel<boolean>('show', { required: true })
const emit = defineEmits<{ saved: [] }>()
const { activeTab, permissions, dataScope, permissionError, scopeError, loading, saving, canPermissions, canScope, permissionReadonly, scopeReadonly, canSave, currentReady, reload, save } = useRoleAuthorization(props, show, emit)
</script>

<template>
  <n-modal v-model:show="show" preset="card" :title="`角色授权 · ${props.role.roleName}`" style="width: 720px; max-width: calc(100vw - 32px); max-height: calc(100dvh - 32px); display: flex; flex-direction: column" content-style="min-height: 0; overflow-y: auto" :mask-closable="!saving" :close-on-esc="!saving" :closable="!saving">
    <n-tabs v-model:value="activeTab" type="line" animated>
      <n-tab-pane v-if="canPermissions" name="permissions" tab="功能权限" display-directive="show" :disabled="saving">
        <n-spin :show="loading">
          <n-alert v-if="permissionError" type="error">{{ permissionError }} <n-button text type="info" @click="reload">重新加载</n-button></n-alert>
          <PermissionForm v-if="permissions" ref="permissionsForm" :role="props.role" :initial-model="permissions" :readonly="permissionReadonly" :disabled="saving" />
        </n-spin>
      </n-tab-pane>
      <n-tab-pane v-if="canScope" name="dataScope" tab="数据范围" display-directive="show" :disabled="saving">
        <n-spin :show="loading">
          <n-alert v-if="scopeError" type="error">{{ scopeError }} <n-button text type="info" @click="reload">重新加载</n-button></n-alert>
          <DataScopeForm v-if="dataScope" ref="dataScopeForm" :initial-model="dataScope" :readonly="scopeReadonly" :disabled="saving" />
        </n-spin>
      </n-tab-pane>
    </n-tabs>
    <template #footer>
      <n-flex justify="end" align="center" :size="12">
        <n-button :disabled="saving" @click="show = false">关闭</n-button>
        <n-button v-if="canSave" type="warning" :loading="saving" :disabled="loading || !currentReady" @click="save">{{ activeTab === 'permissions' ? '保存功能权限' : '保存数据范围' }}</n-button>
      </n-flex>
    </template>
  </n-modal>
</template>

