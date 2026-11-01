<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useIcon } from '@/components/icon/useIcon'
import { useDept } from './hooks/useDept'

defineOptions({ name: 'Dept' })
const { createIcon } = useIcon()
const { registerTable, form, canView, statusOptions, expandableKeys, allExpanded, openAddDialog, toggleExpand } = useDept()
</script>

<template>
  <div>
    <WhimTable v-if="canView" @register="registerTable" @add="openAddDialog()">
      <template #search>
        <n-form-item-gi label="部门名称" path="deptName">
          <n-input v-model:value="form.deptName" :maxlength="64" placeholder="请输入部门名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarExtra>
        <n-button secondary :disabled="!expandableKeys.length" :render-icon="createIcon(allExpanded ? 'chevrons-up' : 'chevrons-down')" @click="toggleExpand">
          {{ allExpanded ? '收起全部' : '展开全部' }}
        </n-button>
      </template>
      <template #empty><n-empty description="暂无部门，请新增或调整查询条件" /></template>
    </WhimTable>
    <n-empty v-else description="暂无部门查询权限" />
  </div>
</template>
