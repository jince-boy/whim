<script setup lang="ts">
import WhimTable from '@/components/table/WhimTable.vue'
import SearchForm from '@/components/form/SearchForm.vue'
import { useOperLog } from '@/views/system/operLog/hooks/useOperLog.ts'
import { useIcon } from '@/components/icon/useIcon.ts'

const { createIcon } = useIcon()

const {
  form,
  handleReset,
  dateRange,
  sysStatusOptions,
  handleDateChange,
  sysOperTypeOptions,
  getOperLogPage,
  tableColumns,
  tableData,
  pagination,
  loading,
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
  <n-space vertical>
    <SearchForm
      :model="form"
      :show-feedback="false"
      label-placement="left"
      @submit="getOperLogPage()"
      @reset="handleReset"
    >
      <template #form>
        <n-form-item-gi label="模块名称">
          <n-input v-model:value="form.title" placeholder="请输入模块名称" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作人">
          <n-input v-model:value="form.operName" placeholder="请输入操作人" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="IP地址">
          <n-input v-model:value="form.ipAddress" placeholder="请输入登录IP地址" clearable />
        </n-form-item-gi>
        <n-form-item-gi label="操作类型">
          <n-select
            placeholder="请选择操作类型"
            v-model:value="form.logType"
            :options="sysOperTypeOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="操作状态">
          <n-select
            placeholder="请选择登录状态"
            v-model:value="form.status"
            :options="sysStatusOptions"
            clearable
          />
        </n-form-item-gi>
        <n-form-item-gi label="登录时间" path="form.type">
          <n-date-picker
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            v-model:value="dateRange"
            type="datetimerange"
            clearable
            @update:value="handleDateChange"
          />
        </n-form-item-gi>
      </template>
    </SearchForm>
    <WhimTable
      :columns="tableColumns"
      :data="tableData"
      :pagination="pagination"
      v-model:page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :loading="loading"
      :delete-button-permission="['system:operLog:delete']"
      :export-button-permission="['system:operLog:export']"
      :add-button-show="false"
      :edit-button-show="false"
      @refresh="getOperLogPage"
      @update:page="getOperLogPage"
      @update:page-size="getOperLogPage"
      @export="exportExcel"
      @delete="removeOperLogBatch"
    >
      <template #action-buttons-left>
        <n-button
          type="error"
          v-permission="['system:operLog:clean']"
          :render-icon="createIcon('shanchu')"
          @click="handleCleanOperLog"
          >清空</n-button
        >
      </template>
    </WhimTable>
    <n-modal
      v-model:show="showModal"
      preset="card"
      title="操作日志详情"
      size="huge"
      style="width: 800px"
      :bordered="false"
    >
      <div>
        <n-descriptions
          size="small"
          label-placement="left"
          label-align="right"
          bordered
          :column="1"
        >
          <!-- 基本信息 -->
          <n-descriptions-item label="模块名称">
            <n-text strong>{{ currentOperLog!.title }}</n-text>
          </n-descriptions-item>

          <n-descriptions-item label="操作人信息">
            <n-space>
              <n-tag size="small" type="info">{{ currentOperLog!.operName }}</n-tag>
              <n-tag size="small">{{ currentOperLog!.operIp }}</n-tag>
              <n-tag size="small" type="success">{{ currentOperLog!.operLocation }}</n-tag>
            </n-space>
          </n-descriptions-item>
          <n-descriptions-item label="日志类型">
            <n-tag
              :type="getDictOperTypeData(String(currentOperLog!.logType))?.listClass"
              size="small"
            >
              {{ getDictOperTypeData(String(currentOperLog!.logType))?.label }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="操作状态">
            <n-tag
              :type="getDictStatusData(String(currentOperLog!.status))?.listClass"
              size="small"
            >
              {{ getDictStatusData(String(currentOperLog!.status))?.label }}
            </n-tag>
          </n-descriptions-item>

          <!-- 请求信息 -->
          <n-descriptions-item label="请求信息">
            <n-space vertical size="small">
              <n-text>地址: {{ currentOperLog!.requestUrl }}</n-text>
              <n-text>方式: {{ currentOperLog!.requestMethod }}</n-text>
              <n-text>方法: {{ currentOperLog!.methodName }}</n-text>
            </n-space>
          </n-descriptions-item>

          <!-- 时间信息 -->
          <n-descriptions-item label="时间信息">
            <n-space vertical size="small">
              <n-text>操作时间: {{ currentOperLog!.operTime }}</n-text>
              <n-text>耗时: {{ currentOperLog!.costTime }}ms</n-text>
            </n-space>
          </n-descriptions-item>

          <!-- 错误信息 -->
          <n-descriptions-item v-if="currentOperLog!.errorMessage" label="错误消息">
            <n-alert type="error" size="small" :bordered="false">
              {{ currentOperLog!.errorMessage }}
            </n-alert>
          </n-descriptions-item>

          <!-- 请求参数 -->
          <n-descriptions-item label="请求参数">
            <n-scrollbar style="max-height: 100px" trigger="none">
              <n-code
                :code="JSON.stringify(JSON.parse(currentOperLog!.requestParam), null, 2)"
                language="json"
                show-line-numbers
              />
            </n-scrollbar>
          </n-descriptions-item>

          <!-- 响应数据 -->
          <n-descriptions-item label="响应数据">
            <n-scrollbar style="max-height: 100px" trigger="none">
              <n-code
                :code="JSON.stringify(JSON.parse(currentOperLog!.responseParam), null, 2)"
                language="json"
                show-line-numbers
              />
            </n-scrollbar>
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </n-space>
</template>

<style scoped lang="scss"></style>
