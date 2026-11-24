<script setup lang="ts">
import { DataTable } from '@/components/dataTable'
import { useHomePage } from './hooks/useHomePage'

defineOptions({ name: 'HomePageManagement' })
const { registerTable, form, editHome, canView, typeOptions, statusOptions } = useHomePage()
</script>

<template>
  <div>
    <DataTable v-if="canView" @register="registerTable" @add="editHome()">
      <template #search>
        <n-form-item-gi label="关联类型" path="targetType">
          <n-select v-model:value="form.targetType" :options="typeOptions" placeholder="全部类型" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #empty><n-empty description="暂无匹配的首页配置" /></template>
    </DataTable>
    <n-empty v-else description="暂无首页管理权限" />
  </div>
</template>
