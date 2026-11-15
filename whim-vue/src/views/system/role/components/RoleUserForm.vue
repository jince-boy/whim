<script setup lang="ts">
import { WhimTable } from '@/components/table'
import type { RoleContextProps } from '../hooks/types'
import { useRoleUserForm } from '../hooks/useRoleUserForm'

const props = defineProps<RoleContextProps>()
const { formRef, formModel, rules, registerTable, searchModel, selectedCount } = useRoleUserForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <div class="role-user-form">
    <WhimTable @register="registerTable">
      <template #search>
        <n-form-item-gi label="用户名" path="username"><n-input v-model:value="searchModel.username" :maxlength="32" clearable placeholder="请输入用户名" /></n-form-item-gi>
        <n-form-item-gi label="姓名" path="name"><n-input v-model:value="searchModel.name" :maxlength="24" clearable placeholder="请输入姓名" /></n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-text strong>可分配用户</n-text>
        <n-tag size="small" type="primary" :bordered="false">已选 {{ selectedCount }} / 100</n-tag>
        <n-text depth="3">搜索、翻页保留勾选</n-text>
      </template>
      <template #empty><n-empty description="暂无符合条件的用户" /></template>
    </WhimTable>
    <n-form ref="formRef" :model="formModel" :rules="rules" :show-label="false" class="role-user-form__hint">
      <n-form-item path="userIds">
        <n-text depth="3">仅展示可分配的用户。分配后，所选用户需要重新登录。</n-text>
      </n-form-item>
    </n-form>
  </div>
</template>

<style scoped>
.role-user-form {
  min-width: 0;
}

.role-user-form__hint {
  margin-top: 12px;
}
</style>
