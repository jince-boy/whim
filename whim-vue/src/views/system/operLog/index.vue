<script setup lang="ts">
import { WhimTable } from '@/components/table'
import { useOperLog } from '@/views/system/operLog/hooks/useOperLog.ts'
import { useIcon } from '@/components/icon/useIcon.ts'

const { createIcon } = useIcon()

const {
  registerTable,
  form,
  canView,
  canClean,
  sysStatusOptions,
  sysOperTypeOptions,
  exportExcel,
  removeOperLogBatch,
  handleCleanOperLog,
  showModal,
  currentOperLog,
  getDictOperTypeData,
  getDictStatusData,
} = useOperLog()
</script>

<template>
  <div>
    <WhimTable v-if="canView" @register="registerTable" @delete="removeOperLogBatch" @export="exportExcel">
      <template #search>
        <n-form-item-gi label="模块名称" path="title">
          <n-input v-model:value="form.title" placeholder="请输入模块名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作人" path="operName">
          <n-input v-model:value="form.operName" placeholder="请输入操作人" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作结果" path="status">
          <n-select
            placeholder="全部状态"
            v-model:value="form.status"
            :options="sysStatusOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="IP 地址" path="ipAddress">
          <n-input v-model:value="form.ipAddress" placeholder="请输入操作 IP 地址" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作类型" path="logType">
          <n-select
            placeholder="请选择操作类型"
            v-model:value="form.logType"
            :options="sysOperTypeOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="操作时间" path="dateRange" :span="2">
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
          type="error"
          v-if="canClean"
          :render-icon="createIcon('trash-2')"
          @click="handleCleanOperLog"
          >清空</n-button
        >
      </template>
    </WhimTable>
    <n-card v-else><n-empty description="暂无操作日志查询权限" /></n-card>
    <n-modal
      v-model:show="showModal"
      preset="card"
      title="操作日志详情"
      size="huge"
      style="width: min(800px, 94vw)"
      :bordered="false"
    >
      <div v-if="currentOperLog">
        <n-descriptions
          size="small"
          label-placement="left"
          label-align="right"
          bordered
          :column="1"
        >
          <!-- 基本信息 -->
          <n-descriptions-item label="模块名称">
            <n-text strong>{{ currentOperLog.title }}</n-text>
          </n-descriptions-item>

          <n-descriptions-item label="操作人信息">
            <n-space>
              <n-tag size="small" type="info">{{ currentOperLog.operName }}</n-tag>
              <n-tag size="small">{{ currentOperLog.operIp }}</n-tag>
              <n-tag size="small" type="success">{{ currentOperLog.operLocation }}</n-tag>
            </n-space>
          </n-descriptions-item>
          <n-descriptions-item label="日志类型">
            <n-tag
              :type="getDictOperTypeData(currentOperLog.logType)?.listClass"
              size="small"
            >
              {{ getDictOperTypeData(currentOperLog.logType)?.label }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="操作结果">
            <n-tag
              :type="getDictStatusData(currentOperLog.status)?.listClass"
              size="small"
            >
              {{ getDictStatusData(currentOperLog.status)?.label }}
            </n-tag>
          </n-descriptions-item>

          <!-- 请求信息 -->
          <n-descriptions-item label="请求信息">
            <n-space vertical size="small">
              <n-text>地址: {{ currentOperLog.requestUrl }}</n-text>
              <n-text>方式: {{ currentOperLog.requestMethod }}</n-text>
              <n-text>方法: {{ currentOperLog.methodName }}</n-text>
            </n-space>
          </n-descriptions-item>

          <!-- 时间信息 -->
          <n-descriptions-item label="时间信息">
            <n-space vertical size="small">
              <n-text>操作时间: {{ currentOperLog.operTime }}</n-text>
              <n-text>耗时: {{ currentOperLog.costTime }}ms</n-text>
            </n-space>
          </n-descriptions-item>

          <!-- 错误信息 -->
          <n-descriptions-item v-if="currentOperLog.errorMessage" label="错误消息">
            <n-alert type="error" size="small" :bordered="false">
              {{ currentOperLog.errorMessage }}
            </n-alert>
          </n-descriptions-item>

          <!-- 请求参数 -->
          <n-descriptions-item label="请求摘要">
            <n-scrollbar style="max-height: 100px" trigger="none">
              <n-code
                :code="currentOperLog.requestParam ?? '—'"
                language="json"
                show-line-numbers
              />
            </n-scrollbar>
          </n-descriptions-item>

          <!-- 响应数据 -->
          <n-descriptions-item label="响应摘要">
            <n-scrollbar style="max-height: 100px" trigger="none">
              <n-code
                :code="currentOperLog.responseParam ?? '—'"
                language="json"
                show-line-numbers
              />
            </n-scrollbar>
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </div>
</template>
