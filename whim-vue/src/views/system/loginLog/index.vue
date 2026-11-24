<script setup lang="ts">
import { DataTable } from '@/components/dataTable'
import { useLoginLog } from '@/views/system/loginLog/hooks/useLoginLog.ts'
import { useIcon } from '@/components/appIcon/hooks/useIcon.ts'

const { createIcon } = useIcon()

const {
  registerTable,
  form,
  canView,
  canClean,
  sysStatusOptions,
  exportExcel,
  removeLoginLogs,
  handleCleanLoginLog,
} = useLoginLog()
</script>

<template>
  <div>
    <DataTable
      v-if="canView"
      @register="registerTable"
      @delete="removeLoginLogs"
      @export="exportExcel"
    >
      <template #search>
        <n-form-item-gi label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="登录结果" path="status">
          <n-select
            placeholder="全部状态"
            v-model:value="form.status"
            :options="sysStatusOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="IP 地址" path="ipAddress">
          <n-input v-model:value="form.ipAddress" placeholder="请输入登录 IP 地址" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="登录时间" path="dateRange" :span="2">
          <n-date-picker
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            v-model:value="form.dateRange"
            type="datetimerange"
            clearable
          />
        </n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-button
          v-if="canClean"
          secondary
          type="error"
          :render-icon="createIcon('trash-2')"
          @click="handleCleanLoginLog"
          >清空</n-button
        >
      </template>
    </DataTable>
    <n-card v-else><n-empty description="暂无登录日志查询权限" /></n-card>
  </div>
</template>
