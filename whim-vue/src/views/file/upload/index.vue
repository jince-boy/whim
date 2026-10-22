<script setup lang="ts">
import { useFileUpload } from './hooks/useFileUpload'

defineOptions({ name: 'FileUpload' })
const {
  options,
  tasks,
  task,
  selectedFile,
  phase,
  phaseLabel,
  running,
  loading,
  errorMessage,
  hashProgress,
  progress,
  transferred,
  page,
  total,
  downloadId,
  formatSize,
  statusLabel,
  chooseFile,
  selectFile,
  startUpload,
  pauseUpload,
  confirmUpload,
  cancelTask,
  downloadTask,
  refreshTasks,
  changePage,
} = useFileUpload()
</script>

<template>
  <n-el class="file-page">
    <div class="file-heading">
      <div>
        <h1>我的文件</h1>
        <p>上传中断后，可重新选择原文件继续上传。</p>
      </div>
      <n-button
        v-permission="['system:fileUpload:upload']"
        type="primary"
        :disabled="running || !options"
        @click="chooseFile()"
        >选择文件</n-button
      >
      <input
        ref="fileInput"
        class="file-input"
        type="file"
        aria-label="选择上传文件"
        @change="selectFile"
      />
    </div>

    <n-alert v-if="errorMessage" type="error" :bordered="false" class="file-alert">{{
      errorMessage
    }}</n-alert>

    <n-card v-if="selectedFile || task" :bordered="false" class="active-upload">
      <div class="file-row">
        <div class="file-name">
          <strong>{{ selectedFile?.name || task?.fileName }}</strong>
          <n-text depth="3">{{ phaseLabel }}</n-text>
        </div>
        <n-space>
          <n-button
            v-if="running && (phase === 'HASHING' || phase === 'UPLOADING')"
            @click="pauseUpload"
            >暂停</n-button
          >
          <n-button
            v-if="!running && selectedFile && (phase === 'PAUSED' || phase === 'ERROR')"
            v-permission="['system:fileUpload:upload']"
            type="primary"
            @click="startUpload"
            >继续上传</n-button
          >
        </n-space>
      </div>
      <n-progress
        type="line"
        :percentage="phase === 'HASHING' ? hashProgress : progress"
        :status="phase === 'ERROR' ? 'error' : phase === 'DONE' ? 'success' : 'default'"
      />
      <n-text depth="3">
        {{
          phase === 'HASHING'
            ? '正在读取文件并确认内容，请稍候'
            : formatSize(transferred) +
              ' / ' +
              formatSize(selectedFile?.size || task?.fileSize || 0)
        }}
      </n-text>
    </n-card>

    <n-card title="上传记录" :bordered="false">
      <template #header-extra
        ><n-button :loading="loading" @click="refreshTasks">刷新</n-button></template
      >
      <n-text v-if="options" depth="3">
        单文件上限
        {{ formatSize(options.maximumFileSize) }}。暂停保留已上传分片，取消会清理临时文件。
      </n-text>
      <n-spin :show="loading">
        <n-empty v-if="!tasks.length" description="暂无上传记录" class="empty-files" />
        <div v-for="item in tasks" :key="item.id" class="file-row file-item">
          <div class="file-name">
            <strong>{{ item.fileName }}</strong>
            <n-text depth="3">{{ formatSize(item.fileSize) }} · {{ item.createTime }}</n-text>
            <n-text v-if="item.status === 'UPLOADING' || item.status === 'CREATED'" depth="3"
              >保留至 {{ item.expiresAt }}</n-text
            >
          </div>
          <n-tag :type="item.status === 'COMPLETED' ? 'success' : 'default'" :bordered="false">{{
            statusLabel(item.status)
          }}</n-tag>
          <n-space>
            <n-button
              v-if="item.status === 'UPLOADING' || item.status === 'CREATED'"
              v-permission="['system:fileUpload:upload']"
              size="small"
              :disabled="running"
              @click="chooseFile(item)"
              >选择原文件续传</n-button
            >
            <n-button
              v-if="item.status === 'COMPLETING'"
              v-permission="['system:fileUpload:upload']"
              size="small"
              :disabled="running"
              @click="confirmUpload(item)"
              >确认结果</n-button
            >
            <n-button
              v-if="item.status === 'COMPLETED'"
              v-permission="['system:fileUpload:download']"
              size="small"
              :disabled="!!downloadId"
              :loading="downloadId === item.id"
              @click="downloadTask(item)"
              >下载</n-button
            >
            <n-popconfirm
              v-if="
                item.status === 'UPLOADING' ||
                item.status === 'CREATED' ||
                item.status === 'CANCELING'
              "
              @positive-click="cancelTask(item)"
            >
              <template #trigger
                ><n-button
                  v-permission="['system:fileUpload:cancel']"
                  size="small"
                  :disabled="running"
                  >取消任务</n-button
                ></template
              >
              取消后将清理已上传分片，确认取消？
            </n-popconfirm>
          </n-space>
        </div>
      </n-spin>
      <n-pagination
        :page="page"
        :page-size="10"
        :item-count="total"
        class="file-pagination"
        @update:page="changePage"
      />
    </n-card>
  </n-el>
</template>

<style scoped lang="scss">
.file-page {
  max-width: 1200px;
  margin: 0 auto;
}
.file-heading,
.file-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.file-heading {
  margin-bottom: 24px;
}
.file-heading h1 {
  margin: 0 0 8px;
  font-size: 24px;
}
.file-heading p {
  margin: 0;
  color: var(--text-color-3);
}
.file-input {
  display: none;
}
.file-alert,
.active-upload {
  margin-bottom: 20px;
}
.active-upload .file-row {
  margin-bottom: 16px;
}
.file-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--divider-color);
}
.file-name {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
  overflow-wrap: anywhere;
}
.file-pagination {
  justify-content: flex-end;
  margin-top: 20px;
}
.empty-files {
  padding: 40px;
}
@media (max-width: 720px) {
  .file-row {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }
  .file-name {
    flex-basis: 100%;
  }
}
</style>
