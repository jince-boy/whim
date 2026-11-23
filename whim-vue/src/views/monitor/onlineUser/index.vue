<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useOnlineUser } from './hooks/useOnlineUser'

const { registerTable, form, canView, totals } = useOnlineUser()
</script>

<template>
  <div>
    <WhimTable v-if="canView" @register="registerTable">
      <template #search>
        <n-form-item-gi label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" :maxlength="64" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="主机 IP" path="ipAddress">
          <n-input v-model:value="form.ipAddress" placeholder="请输入主机 IP" :maxlength="64" clearable />
        </n-form-item-gi>
      </template>
      <template #toolbarBefore>
        <n-text strong>在线用户</n-text>
        <n-text v-if="totals" depth="3" aria-live="polite">
          {{ totals.accounts }} 个账号 · {{ totals.sessions }} 个有效会话
        </n-text>
      </template>
    </WhimTable>
    <n-card v-else><n-empty description="暂无在线用户查询权限" /></n-card>
  </div>
</template>
