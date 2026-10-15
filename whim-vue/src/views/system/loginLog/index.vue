<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useLoginLog } from '@/views/system/loginLog/hooks/useLoginLog.ts'
import { useIcon } from '@/components/icon/useIcon.ts'

const { createIcon } = useIcon()

const {
  registerTable,
  form,
  sysStatusOptions,
  exportExcel,
  removeLoginLogBatch,
  handleCleanLoginLog,
} = useLoginLog()
</script>

<template>
  <n-space vertical>
    <WhimTable @register="registerTable" @delete="removeLoginLogBatch" @export="exportExcel">
      <template #search>
        <n-form-item-gi label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="IP地址" path="ipAddress">
          <n-input v-model:value="form.ipAddress" placeholder="请输入登录IP地址" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="登录状态" path="status">
          <n-select
            placeholder="请选择登录状态"
            v-model:value="form.status"
            :options="sysStatusOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="登录时间" path="dateRange">
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
          v-permission="['system:loginLog:clean']"
          type="error"
          :render-icon="createIcon('shanchu')"
          @click="handleCleanLoginLog"
          >清空</n-button
        >
      </template>
    </WhimTable>
  </n-space>
</template>

<style scoped lang="scss"></style>
