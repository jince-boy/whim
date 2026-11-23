<script setup lang="ts">
import { WhimTable } from '@/components/table'
import NoticeDetail from './components/NoticeDetail.vue'
import { useNotice } from './hooks/useNotice'

defineOptions({ name: 'SysNotice' })
const { registerTable, form, canView, types, statuses, openNoticeForm, detail, showDetail } = useNotice()
</script>

<template>
  <div>
    <WhimTable v-if="canView" @register="registerTable" @add="openNoticeForm()">
      <template #search>
        <n-form-item-gi label="标题" path="title">
          <n-input v-model:value="form.title" :maxlength="100" placeholder="搜索公告标题" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="类型" path="noticeType">
          <n-select v-model:value="form.noticeType" :options="types" placeholder="全部类型" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="状态" path="status">
          <n-select v-model:value="form.status" :options="statuses" placeholder="全部状态" clearable />
        </n-form-item-gi>
      </template>
      <template #empty><n-empty description="暂无符合条件的公告" /></template>
    </WhimTable>
    <n-card v-else><n-empty description="暂无公告管理权限" /></n-card>
    <NoticeDetail v-model:show="showDetail" :notice="detail" />
  </div>
</template>
