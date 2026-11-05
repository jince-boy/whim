<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { createIcon } from '@/components/icon/useIcon'
import { usePost } from './hooks/usePost'

defineOptions({ name: 'Post' })
const {
  registerTable, form, canView, detail, showDetail,
  canLoadDepartments, departmentTree, departmentPattern, departmentLoading, departmentError,
  selectedDepartmentKeys, selectDepartment, reloadDepartments, departmentName, statusOptions,
  openAddDialog, getStatusData,
} = usePost()
</script>

<template>
  <div>
    <div v-if="canView" class="post-layout">
      <n-card class="department-panel" size="medium" content-style="padding: 10px" :theme-overrides="{ borderRadius: '8px' }">
        <n-flex class="department-toolbar" align="center" justify="space-between" :size="8">
          <n-text strong>部门</n-text>
          <n-tooltip>
            <template #trigger>
              <n-button quaternary size="small" aria-label="刷新部门" :render-icon="createIcon('refresh-cw')" :loading="departmentLoading" :disabled="!canLoadDepartments" @click="reloadDepartments" />
            </template>
            刷新
          </n-tooltip>
        </n-flex>
        <n-space vertical :size="10">
          <n-input v-model:value="departmentPattern" placeholder="搜索部门" clearable :disabled="!canLoadDepartments" />
          <n-alert v-if="departmentError" type="error">{{ departmentError }}</n-alert>
          <n-text v-else-if="!canLoadDepartments" depth="3">暂无部门查询权限</n-text>
          <n-spin :show="departmentLoading">
            <n-tree
              :data="departmentTree"
              :pattern="departmentPattern"
              :selected-keys="selectedDepartmentKeys"
              :cancelable="false"
              block-line
              default-expand-all
              @update:selected-keys="selectDepartment"
            />
          </n-spin>
        </n-space>
      </n-card>
      <div class="post-table">
        <WhimTable @register="registerTable" @add="openAddDialog">
          <template #search>
            <n-form-item-gi label="岗位名称" path="postName">
              <n-input v-model:value="form.postName" :maxlength="64" placeholder="请输入岗位名称" clearable />
            </n-form-item-gi>
            <n-form-item-gi label="岗位编码" path="postCode">
              <n-input v-model:value="form.postCode" :maxlength="64" placeholder="请输入岗位编码" clearable />
            </n-form-item-gi>
            <n-form-item-gi label="状态" path="status">
              <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
            </n-form-item-gi>
          </template>
          <template #empty><n-empty description="暂无岗位，请新增或调整查询条件" /></template>
        </WhimTable>
      </div>
    </div>
    <n-empty v-else description="暂无岗位查询权限" />
    <n-modal v-model:show="showDetail" preset="card" title="岗位详情" :style="{ width: '640px', maxWidth: 'calc(100vw - 32px)', maxHeight: 'calc(100dvh - 32px)', overflowY: 'auto' }" :mask-closable="true">
      <n-descriptions v-if="detail" :column="1" label-placement="left" bordered>
        <n-descriptions-item label="岗位名称">{{ detail.postName }}</n-descriptions-item>
        <n-descriptions-item label="岗位编码">{{ detail.postCode }}</n-descriptions-item>
        <n-descriptions-item label="岗位 ID">{{ detail.id }}</n-descriptions-item>
        <n-descriptions-item label="所属部门">{{ departmentName(detail.deptId) }}</n-descriptions-item>
        <n-descriptions-item label="显示排序">{{ detail.sort }}</n-descriptions-item>
        <n-descriptions-item label="状态"><n-tag :type="getStatusData(detail.status).listClass" :bordered="false">{{ getStatusData(detail.status).label }}</n-tag></n-descriptions-item>
        <n-descriptions-item label="备注">{{ detail.remark || '—' }}</n-descriptions-item>
        <n-descriptions-item label="创建时间">{{ detail.createTime || '—' }}</n-descriptions-item>
        <n-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<style scoped>
.post-layout {
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

.post-table {
  min-width: 0;
}

@media (max-width: 900px) {
  .post-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .department-panel {
    max-height: 320px;
  }
}
</style>
