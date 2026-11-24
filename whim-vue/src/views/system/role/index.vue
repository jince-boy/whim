<script setup lang="ts">
import { DataTable } from '@/components/dataTable'
import { useRole } from './hooks/useRole'
import RoleAuthorizationDialog from './components/RoleAuthorizationDialog.vue'
import RoleUsersDrawer from './components/RoleUsersDrawer.vue'

defineOptions({ name: 'Role' })
const { registerTable, form, canView, openAddDialog, statusOptions, authorizingRole, showAuthorization, usersRole, showUsers, reloadRoles } = useRole()
</script>

<template>
  <div class="role-page">
    <DataTable v-if="canView" @register="registerTable" @add="openAddDialog">
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
    </DataTable>
    <n-card v-else :bordered="false"><n-empty description="暂无角色查询权限" /></n-card>
    <RoleAuthorizationDialog v-if="authorizingRole" v-model:show="showAuthorization" :role="authorizingRole" @saved="reloadRoles" />
    <RoleUsersDrawer v-if="usersRole" v-model:show="showUsers" :role="usersRole" />
  </div>
</template>
