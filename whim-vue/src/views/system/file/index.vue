<script setup lang="ts">
import { DataTable } from '@/components/dataTable'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { pageTabThemeOverrides } from '@/config/NaiveTheme'
import FileUploadProgress from './components/FileUploadProgress.vue'
import { useFile } from './hooks/useFile'
defineOptions({ name: 'SysFile' })
const { canView, canStorage, canUpload, canDelete, activeTab, registerFiles, registerStorage,
  form, storageOptions, platformOptions, uploadSourceOptions, deleting, downloadingId, selectedFiles,
  uploads, uploadSummary, selectUploadFiles, addUploadFiles, pauseUpload, resumeUpload, cancelUpload,
  pauseAllUploads, resumeAllUploads, clearFinishedUploads,
  uploadLimit, formatSize, addStorage, loadUploadLimit, removeSelectedFiles } = useFile()
</script>

<template>
  <div>
    <input ref="uploadInput" type="file" multiple hidden @change="addUploadFiles" />
    <n-tabs v-model:value="activeTab" class="whim-page-tabs" type="segment" size="small" animated
      :theme-overrides="pageTabThemeOverrides">
      <n-tab-pane v-if="canView" name="files" tab="文件列表" display-directive="show">
        <template #tab>
          <AppIcon name="folder-open" />文件列表
        </template>
        <DataTable @register="registerFiles">
          <template #search>
            <n-form-item-gi label="文件名称" path="fileName">
              <n-input v-model:value="form.fileName" :maxlength="512" clearable placeholder="搜索存储文件名称" />
            </n-form-item-gi>
            <n-form-item-gi label="原文件名称" path="originalName">
              <n-input v-model:value="form.originalName" :maxlength="255" clearable placeholder="搜索原文件名称" />
            </n-form-item-gi>
            <n-form-item-gi v-if="canStorage" label="存储配置" path="storageId">
              <n-select v-model:value="form.storageId" :options="storageOptions" clearable placeholder="全部存储" />
            </n-form-item-gi>
            <n-form-item-gi label="文件后缀" path="fileSuffix">
              <n-input v-model:value="form.fileSuffix" :maxlength="255" clearable placeholder="如 png、pdf、docx" />
            </n-form-item-gi>
            <n-form-item-gi label="存储类型" path="platform">
              <n-select v-model:value="form.platform" :options="platformOptions" clearable placeholder="全部存储类型" />
            </n-form-item-gi>
            <n-form-item-gi label="上传途径" path="uploadSource">
              <n-select v-model:value="form.uploadSource" :options="uploadSourceOptions" clearable placeholder="全部上传途径" />
            </n-form-item-gi>
            <n-form-item-gi label="上传时间" path="dateRange" span="1 520:2">
              <n-date-picker v-model:value="form.dateRange" type="datetimerange" clearable
                start-placeholder="开始时间" end-placeholder="结束时间" />
            </n-form-item-gi>
          </template>
          <template #toolbarBefore>
            <n-button v-if="canUpload" key="upload" type="primary" :disabled="!uploadLimit" @click="selectUploadFiles">
              <template #icon><AppIcon name="upload" /></template>上传文件
            </n-button>
            <FileUploadProgress v-if="canUpload" key="uploadProgress" :uploads="uploads" :summary="uploadSummary"
              @pause="pauseUpload" @resume="resumeUpload" @cancel="cancelUpload"
              @pause-all="pauseAllUploads" @resume-all="resumeAllUploads" @clear-finished="clearFinishedUploads" />
            <n-button v-if="canUpload && !uploadLimit" key="uploadLimit" secondary type="info" @click="loadUploadLimit">
              <template #icon><AppIcon name="refresh-cw" /></template>重新获取上传限制
            </n-button>
            <n-button v-if="canDelete" key="delete" class="whim-button--with-icon" secondary type="error" :loading="deleting"
              :disabled="!selectedFiles.length || deleting || !!downloadingId" @click="removeSelectedFiles">
              <template #icon><AppIcon name="trash-2" /></template>删除
            </n-button>
          </template>
          <template #toolbarExtra>
            <n-text v-if="canUpload && uploadLimit" depth="3">单文件不超过 {{ formatSize(uploadLimit) }}</n-text>
          </template>
          <template #empty><n-empty description="暂无文件，可上传文件或调整筛选条件" /></template>
        </DataTable>
      </n-tab-pane>
      <n-tab-pane v-if="canStorage" name="storage" tab="存储配置" display-directive="show">
        <template #tab>
          <AppIcon name="database" />存储配置
        </template>
        <DataTable @register="registerStorage" @add="addStorage">
          <template #empty><n-empty description="尚未配置存储，请新增本地或对象存储配置" /></template>
        </DataTable>
      </n-tab-pane>
    </n-tabs>
    <n-empty v-if="!canView && !canStorage" description="暂无文件管理权限" />
  </div>
</template>
