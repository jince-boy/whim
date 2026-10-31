<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useMenu } from '@/views/system/menu/hooks/useMenu'
import { useIcon } from '@/components/icon/useIcon'

defineOptions({ name: 'SysMenu' })
const { createIcon } = useIcon()
const {
  registerTable, form, canView, allExpanded, expandableKeys,
  menuTypeOptions, menuStatusOptions, openAddDialog, toggleExpand,
} = useMenu()
</script>

<template>
  <WhimTable v-if="canView" @register="registerTable" @add="openAddDialog()">
    <template #search>
      <n-form-item-gi label="菜单名称" path="menuName">
        <n-input v-model:value="form.menuName" placeholder="请输入菜单名称" :maxlength="64" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="菜单类型" path="menuType">
        <n-select v-model:value="form.menuType" :options="menuTypeOptions" placeholder="全部类型" clearable />
      </n-form-item-gi>
      <n-form-item-gi label="菜单状态" path="status">
        <n-select v-model:value="form.status" :options="menuStatusOptions" placeholder="全部状态" clearable />
      </n-form-item-gi>
    </template>
    <template #toolbarExtra>
      <n-button secondary :disabled="!expandableKeys.length" :render-icon="createIcon(allExpanded ? 'chevrons-up' : 'chevrons-down')" @click="toggleExpand">
        {{ allExpanded ? '收起全部' : '展开全部' }}
      </n-button>
    </template>
    <template #empty><n-empty description="暂无菜单，请新增或调整查询条件" /></template>
  </WhimTable>
  <n-empty v-else description="暂无菜单查询权限" />
</template>
