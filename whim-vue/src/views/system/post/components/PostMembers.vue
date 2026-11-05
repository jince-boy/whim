<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { usePostMembers } from '../hooks/usePostMembers'
import type { PostMembersProps } from '../hooks/types'

const props = defineProps<PostMembersProps>()
const { registerTable, form, statusOptions } = usePostMembers(props)
</script>

<template>
  <n-space vertical :size="16">
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
        <n-form-item-gi label="用户状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #empty><n-empty description="暂无岗位成员" /></template>
    </WhimTable>
  </n-space>
</template>
