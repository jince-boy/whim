<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useRole } from './hooks/useRole'

defineOptions({ name: 'Role' })
const { registerTable, form, canView, openAddDialog, statusOptions } = useRole()
</script>

<template>
  <div class="role-page">
    <WhimTable v-if="canView" @register="registerTable" @add="openAddDialog">
      <template #search>
        <n-form-item-gi label="角色名称" path="roleName">
          <n-input v-model:value="form.roleName" :maxlength="32" placeholder="请输入角色名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="角色编码" path="roleCode">
          <n-input v-model:value="form.roleCode" :maxlength="128" placeholder="请输入角色编码" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #empty><n-empty description="暂无角色，请新增或调整查询条件" /></template>
    </WhimTable>
    <n-card v-else :bordered="false"><n-empty description="暂无角色查询权限" /></n-card>
  </div>
</template>
