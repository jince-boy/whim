<script setup lang="ts">
import { WhimTable } from '@/components/table'
import type { RoleFormProps } from './hooks/types'
import { useRoleForm } from './hooks/useRoleForm'

const props = defineProps<RoleFormProps>()
const { formRef, formModel, rules, registerTable, searchModel, selectedCount } = useRoleForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <div class="role-form">
    <WhimTable @register="registerTable">
      <template #search>
        <n-form-item-gi label="角色名称" path="roleName"><n-input v-model:value="searchModel.roleName" clearable placeholder="请输入角色名称" /></n-form-item-gi>
        <n-form-item-gi label="角色编码" path="roleCode"><n-input v-model:value="searchModel.roleCode" clearable placeholder="请输入角色编码" /></n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-text strong>可分配角色</n-text>
        <n-tag size="small" type="primary" :bordered="false">已选 {{ selectedCount }} / 100</n-tag>
        <n-text depth="3">搜索、翻页保留勾选</n-text>
      </template>
      <template #empty><n-empty description="暂无符合条件的角色" /></template>
    </WhimTable>
    <n-form ref="formRef" :model="formModel" :rules="rules" :show-label="false" class="role-form__hint">
      <n-form-item path="roleIds">
        <n-text depth="3">角色决定功能权限与数据范围；未选角色时不授予角色权限。保存后，该用户需要重新登录。</n-text>
      </n-form-item>
    </n-form>
  </div>
</template>

<style scoped>
.role-form {
  min-width: 0;
}

.role-form__hint {
  margin-top: 12px;
}
</style>

