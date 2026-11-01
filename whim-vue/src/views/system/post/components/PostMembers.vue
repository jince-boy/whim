<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import { usePostMembers } from '../hooks/usePostMembers'
import type { PostMembersProps } from '../hooks/types'

const props = defineProps<PostMembersProps>()
const busy = defineModel<boolean>('busy', { required: true })
const { createIcon } = useIcon()
const { registerTable, form, assigned, canAssign, canAdd, selectedCount, loading, statusOptions, changeSelectedMembers } = usePostMembers(props, busy)
</script>

<template>
  <n-space vertical :size="16">
    <n-alert v-if="props.post.status === 1" type="warning">该岗位已停用，可以查看和移除成员，启用后才能添加成员。</n-alert>
    <n-radio-group v-if="canAdd" v-model:value="assigned" :disabled="busy" aria-label="成员分配状态">
      <n-radio-button :value="true">已分配成员</n-radio-button>
      <n-radio-button :value="false">可添加用户</n-radio-button>
    </n-radio-group>
    <WhimTable @register="registerTable">
      <template #search>
        <n-form-item-gi label="用户名" path="username">
          <n-input v-model:value="form.username" :maxlength="32" placeholder="请输入用户名" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="姓名" path="name">
          <n-input v-model:value="form.name" :maxlength="24" placeholder="请输入姓名" clearable />
        </n-form-item-gi>
        <n-form-item-gi v-if="props.canLoadDepartments" label="主部门" path="deptId">
          <n-tree-select v-model:value="form.deptId" :options="props.departmentOptions" filterable clearable placeholder="全部部门" />
        </n-form-item-gi>
        <n-form-item-gi v-if="assigned" label="用户状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-button v-if="canAssign" :type="assigned ? 'error' : 'primary'" :secondary="assigned" :disabled="!selectedCount || busy || loading" :loading="busy" :render-icon="createIcon(assigned ? 'user-minus' : 'user-plus')" @click="changeSelectedMembers">
          {{ assigned ? '移除所选' : '添加所选' }}
        </n-button>
        <n-text v-if="canAssign" depth="3">已选择 {{ selectedCount }} 名用户</n-text>
      </template>
      <template #empty><n-empty :description="assigned ? '暂无岗位成员' : '暂无可添加的正常用户'" /></template>
    </WhimTable>
  </n-space>
</template>
