<script setup lang="ts">
import { DataTable } from '@/components/dataTable'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import type { RoleContextProps } from '../hooks/types'
import { useRoleUsers } from '../hooks/useRoleUsers'

const props = defineProps<RoleContextProps>()
const show = defineModel<boolean>('show', { required: true })
const { registerMembers, memberForm, canAssign, assigning, removing, removalPending, selectedMemberIds, openCandidates, removeUsers } = useRoleUsers(props, show)
</script>

<template>
  <n-drawer v-model:show="show" width="min(1040px, 100vw)" :mask-closable="!assigning && !removing" :close-on-esc="!assigning && !removing">
    <n-drawer-content :title="`分配用户 · ${props.role.roleName}`" :closable="!assigning && !removing">
      <DataTable @register="registerMembers">
        <template #search>
          <n-form-item-gi label="用户名" path="username"><n-input v-model:value="memberForm.username" :maxlength="32" clearable placeholder="请输入用户名" /></n-form-item-gi>
          <n-form-item-gi label="姓名" path="name"><n-input v-model:value="memberForm.name" :maxlength="24" clearable placeholder="请输入姓名" /></n-form-item-gi>
        </template>
        <template v-if="canAssign" #toolbarBefore>
          <n-button type="primary" :disabled="props.role.status !== 0 || removalPending" @click="openCandidates"><template #icon><AppIcon name="users" /></template>分配用户</n-button>
          <n-button type="error" secondary :loading="removing" :disabled="!selectedMemberIds.length || removalPending" @click="removeUsers(selectedMemberIds)"><template #icon><AppIcon name="trash-2" /></template>批量移除</n-button>
        </template>
        <template #empty><n-empty description="暂无已分配用户" /></template>
      </DataTable>
    </n-drawer-content>
  </n-drawer>
</template>

