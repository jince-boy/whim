<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useMenu } from '@/views/system/menu/hooks/useMenu'

defineOptions({ name: 'SysMenu' })
const { registerTable, form, openAddDialog, toggleExpand } = useMenu()
</script>

<template>
  <WhimTable @register="registerTable" @add="openAddDialog()">
    <template #search>
      <n-form-item-gi label="菜单名称" path="menuName">
        <n-input v-model:value="form.menuName" placeholder="请输入菜单名称" clearable />
      </n-form-item-gi>
    </template>
    <template #toolbarExtra>
      <n-button @click="toggleExpand">展开/收起</n-button>
    </template>
  </WhimTable>
</template>
