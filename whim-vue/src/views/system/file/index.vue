<script setup lang="ts">
import { WhimTable } from '@/components/table'
import Icon from '@/components/icon/Icon.vue'
import { useFile } from './hooks/useFile'
defineOptions({ name: 'SysFile' })
const { themeVars, tabThemeOverrides, canView, canStorage, canUpload, canDelete, canDownload, activeTab, registerFiles, registerStorage,
  form, storageOptions, uploading, uploadProgress, deleting, downloadingId, selectedFiles,
  uploadLimit, formatSize, addStorage, upload, loadUploadLimit, removeSelectedFiles, downloadSelectedFile } = useFile()
</script>

<template>
  <n-tabs v-model:value="activeTab" class="file-tabs" type="segment" animated :theme-overrides="tabThemeOverrides"
    :style="{ '--file-tabs-background': themeVars.cardColor, '--file-tabs-border-color': themeVars.borderColor }">
    <n-tab-pane v-if="canView" name="files" tab="文件列表" display-directive="show">
      <template #tab>
        <span class="file-tab-label"><Icon name="folder-open" />文件列表</span>
      </template>
      <WhimTable @register="registerFiles">
        <template #search>
          <n-form-item-gi label="文件名称" path="originalName">
            <n-input v-model:value="form.originalName" :maxlength="255" clearable placeholder="搜索文件名称" />
          </n-form-item-gi>
          <n-form-item-gi v-if="canStorage" label="存储配置" path="storageId">
            <n-select v-model:value="form.storageId" :options="storageOptions" clearable placeholder="全部存储" />
          </n-form-item-gi>
        </template>
        <template #toolbarBefore>
          <template v-if="canUpload">
            <n-upload class="file-upload" :show-file-list="false" :custom-request="upload" :disabled="uploading || !uploadLimit">
              <n-button type="primary" :loading="uploading" :disabled="!uploadLimit">
                <template #icon><Icon name="upload" /></template>上传文件
              </n-button>
            </n-upload>
            <n-button v-if="!uploadLimit" secondary type="info" @click="loadUploadLimit">
              <template #icon><Icon name="refresh-cw" /></template>重新获取上传限制
            </n-button>
          </template>
          <n-button v-if="canDelete" secondary type="error" :loading="deleting"
            :disabled="!selectedFiles.length || deleting || !!downloadingId" @click="removeSelectedFiles">
            <template #icon><Icon name="trash-2" /></template>删除
          </n-button>
          <n-button v-if="canDownload" secondary type="info" :loading="!!downloadingId"
            :disabled="selectedFiles.length !== 1 || deleting || !!downloadingId" title="勾选一个文件后下载" @click="downloadSelectedFile">
            <template #icon><Icon name="download" /></template>下载
          </n-button>
        </template>
        <template #toolbarExtra>
          <n-text v-if="canUpload && uploadLimit" depth="3">单文件不超过 {{ formatSize(uploadLimit) }}</n-text>
          <n-text v-if="uploading" depth="3">{{ uploadProgress < 100 ? `已传输 ${uploadProgress}%` : '正在保存文件…' }}</n-text>
        </template>
        <template #empty><n-empty description="暂无文件，可上传文件或调整筛选条件" /></template>
      </WhimTable>
    </n-tab-pane>
    <n-tab-pane v-if="canStorage" name="storage" tab="存储配置" display-directive="show">
      <template #tab>
        <span class="file-tab-label"><Icon name="database" />存储配置</span>
      </template>
      <n-alert type="info" :show-icon="false" class="storage-hint">
        首个配置自动设为默认配置。所有新文件统一使用默认配置，切换后立即生效，历史文件继续从原配置下载。
      </n-alert>
      <WhimTable @register="registerStorage" @add="addStorage">
        <template #empty><n-empty description="尚未配置存储，请新增本地或对象存储配置" /></template>
      </WhimTable>
    </n-tab-pane>
  </n-tabs>
  <n-empty v-if="!canView && !canStorage" description="暂无文件管理权限" />
</template>

<style scoped>
.file-tabs :deep(> .n-tabs-nav) {
  padding: 8px;
  background-color: var(--file-tabs-background);
  border: 1px solid var(--file-tabs-border-color);
  border-radius: 8px;
  transition: background-color 0.3s, border-color 0.3s;
}

.file-tabs :deep(> .n-tabs-nav > .n-tabs-rail) {
  width: 288px;
  max-width: 100%;
  box-sizing: border-box;
}

.file-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.storage-hint { margin-bottom: 12px; }
.file-upload { width: auto; }
</style>
