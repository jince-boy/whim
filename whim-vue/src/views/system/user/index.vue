<script setup lang="ts">
import { WhimTable } from '@/components/table'
import Icon from '@/components/icon/Icon.vue'
import { useUser } from './hooks/useUser'

defineOptions({
  name: 'SysUser',
})
const {
  registerTable, form, canView, canLoadDepartments,
  departmentTree, departmentPattern, departmentLoading, departmentError,
  selectedDepartmentKeys, selectDepartment, statusOptions,
  departmentRefreshing, refreshDepartments, finishDepartmentRefreshRotation, openAddDialog,
} = useUser()
</script>

<template>
  <div>
    <div v-if="canView" class="user-layout">
      <n-card class="department-panel" size="medium" content-style="padding: 10px" :theme-overrides="{ borderRadius: '8px' }">
        <n-flex class="department-toolbar" align="center" justify="space-between" :size="8">
          <n-text strong>部门</n-text>
          <n-tooltip>
            <template #trigger>
              <n-button quaternary native-focus-behavior size="small" aria-label="刷新部门" :aria-busy="departmentRefreshing || departmentLoading" :disabled="!canLoadDepartments || departmentRefreshing || departmentLoading" @mousedown.prevent @click="refreshDepartments">
                <template #icon>
                  <Icon name="refresh-cw" :class="{ 'refresh-icon--spinning': departmentRefreshing }" @animationiteration="finishDepartmentRefreshRotation" />
                </template>
              </n-button>
            </template>
            刷新
          </n-tooltip>
        </n-flex>
        <n-space vertical :size="10">
          <n-input v-model:value="departmentPattern" placeholder="搜索部门" clearable :disabled="!canLoadDepartments" />
          <n-alert v-if="departmentError" type="error">{{ departmentError }}</n-alert>
          <n-text v-else-if="!canLoadDepartments" depth="3">暂无部门查询权限</n-text>
          <n-spin :show="departmentLoading">
            <n-tree :data="departmentTree" :pattern="departmentPattern" :selected-keys="selectedDepartmentKeys" :cancelable="false" block-line default-expand-all @update:selected-keys="selectDepartment" />
          </n-spin>
        </n-space>
      </n-card>
      <div class="user-table">
        <WhimTable @register="registerTable" @add="openAddDialog">
          <template #search>
            <n-form-item-gi label="用户名" path="username">
              <n-input v-model:value="form.username" :maxlength="32" placeholder="请输入用户名" clearable />
            </n-form-item-gi>
            <n-form-item-gi label="姓名" path="name">
              <n-input v-model:value="form.name" :maxlength="24" placeholder="请输入姓名" clearable />
            </n-form-item-gi>
            <n-form-item-gi label="联系电话" path="phone">
              <n-input v-model:value="form.phone" :maxlength="20" placeholder="请输入联系电话" clearable />
            </n-form-item-gi>
            <n-form-item-gi label="状态" path="status">
              <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
            </n-form-item-gi>
          </template>
          <template #empty><n-empty description="暂无用户，请新增或调整查询条件" /></template>
        </WhimTable>
      </div>
    </div>
    <n-empty v-else description="暂无用户查询权限" />
  </div>
</template>

<style scoped>
.user-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  align-items: stretch;
  gap: 10px;
}

.department-panel {
  min-width: 0;
  max-height: calc(100dvh - 160px);
  overflow: auto;
}

.department-toolbar {
  min-height: 34px;
  margin-bottom: 10px;
}

.refresh-icon--spinning {
  animation: refresh-rotate 0.6s linear infinite;
}

@keyframes refresh-rotate {
  to { transform: rotate(360deg); }
}

.user-table { min-width: 0; }

@media (max-width: 900px) {
  .user-layout { grid-template-columns: minmax(0, 1fr); }
  .department-panel { max-height: 320px; }
}
</style>
