<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import type { FileUploadView, FileUploadSummary } from '../hooks/types'

const props = defineProps<{ uploads: FileUploadView[]; summary: FileUploadSummary }>()
const emit = defineEmits<{
  pause: [id: string]
  resume: [id: string]
  cancel: [id: string]
  pauseAll: []
  resumeAll: []
  clearFinished: []
}>()
</script>

<template>
  <n-popover trigger="click" placement="bottom-start" :show-arrow="false"
    :style="{ padding: '0', width: '440px', maxWidth: 'calc(100vw - 32px)' }">
    <template #trigger>
      <n-button secondary type="info">
        <template #icon><Icon name="list" /></template>
        上传进度
        <n-badge v-if="props.summary.active > 0" :value="props.summary.active" :max="99" class="upload-count" />
      </n-button>
    </template>
    <section class="upload-panel" aria-label="文件上传进度">
      <div class="upload-panel-header">
        <div>
          <n-text strong>上传进度</n-text>
          <n-text depth="3" class="upload-summary">
            {{ props.summary.active }} 个进行中 · {{ props.summary.finished }} 个已完成
            <template v-if="props.summary.failed"> · {{ props.summary.failed }} 个失败</template>
          </n-text>
        </div>
        <div class="upload-panel-actions">
          <n-button v-if="props.summary.pausable" text size="small" @click="emit('pauseAll')">
            <template #icon><Icon name="pause" /></template>全部暂停
          </n-button>
          <n-button v-if="props.summary.resumable" text size="small" type="info" @click="emit('resumeAll')">
            <template #icon><Icon name="play" /></template>全部继续
          </n-button>
        </div>
      </div>
      <n-divider class="upload-divider" />
      <div class="upload-task-list">
        <article v-for="task in props.uploads" :key="task.id" class="upload-task">
          <div class="upload-task-heading">
            <n-icon size="22" class="upload-file-icon"><Icon name="file" /></n-icon>
            <n-text class="upload-file-name" :title="task.name">{{ task.name }}</n-text>
            <div class="upload-task-actions">
              <n-button v-if="task.state === 'uploading' || task.state === 'waiting'" text size="small"
                :aria-label="`暂停上传 ${task.name}`" @click="emit('pause', task.id)">
                <template #icon><Icon name="pause" /></template>暂停
              </n-button>
              <n-button v-if="task.state === 'paused'" text size="small" type="info"
                :aria-label="`继续上传 ${task.name}`" @click="emit('resume', task.id)">
                <template #icon><Icon name="play" /></template>继续
              </n-button>
              <n-button v-if="task.state === 'error'" text size="small" type="info"
                :aria-label="`重试上传 ${task.name}`" @click="emit('resume', task.id)">
                <template #icon><Icon name="rotate-cw" /></template>重试
              </n-button>
              <n-button v-if="task.state === 'cancelFailed'" text size="small" type="error"
                @click="emit('cancel', task.id)">重试取消</n-button>
              <n-button v-else-if="['waiting', 'uploading', 'paused', 'error'].includes(task.state)"
                text size="small" type="error" :aria-label="`取消上传 ${task.name}`"
                @click="emit('cancel', task.id)">
                <template #icon><Icon name="x" /></template>
              </n-button>
              <n-text v-if="task.state === 'success'" type="success"><n-icon size="18"><Icon name="check" /></n-icon></n-text>
            </div>
          </div>
          <n-progress type="line" :percentage="task.progress" :status="task.progressStatus"
            :show-indicator="false" :height="4" :processing="task.state === 'uploading' || task.state === 'completing'" />
          <div class="upload-task-meta">
            <n-text depth="3">{{ task.transferredLabel }} / {{ task.sizeLabel }}</n-text>
            <n-text :type="task.state === 'error' || task.state === 'cancelFailed' ? 'error' : 'default'">
              {{ task.stateLabel }}<template v-if="task.state === 'uploading'"> · {{ task.progress }}%</template>
            </n-text>
          </div>
          <n-text v-if="task.error" type="error" class="upload-error">{{ task.error }}</n-text>
        </article>
        <n-empty v-if="!props.uploads.length" description="暂无上传任务" class="upload-empty" />
      </div>
      <n-divider class="upload-divider" />
      <div class="upload-panel-footer">
        <n-text depth="3">收起面板后继续上传</n-text>
        <n-button text size="small" :disabled="!props.summary.clearable" @click="emit('clearFinished')">
          清理已结束
        </n-button>
      </div>
    </section>
  </n-popover>
</template>

<style scoped>
.upload-count { margin-left: 8px; }
.upload-panel { min-width: 0; }
.upload-panel-header { padding: 16px 18px 12px; }
.upload-summary { display: block; margin-top: 4px; font-size: 12px; }
.upload-panel-actions { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 12px; }
.upload-panel-actions:empty { display: none; }
.upload-divider { margin: 0; }
.upload-task-list { max-height: min(400px, 60vh); overflow-y: auto; overscroll-behavior: contain; }
.upload-task { padding: 14px 18px; }
.upload-task + .upload-task { padding-top: 4px; }
.upload-task-heading { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.upload-file-icon { flex-shrink: 0; opacity: .6; }
.upload-file-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 500; }
.upload-task-actions { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.upload-task-meta { display: flex; justify-content: space-between; gap: 12px; margin-top: 7px; font-size: 12px; }
.upload-error { display: block; margin-top: 6px; overflow-wrap: anywhere; font-size: 12px; }
.upload-panel-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 18px; font-size: 12px; }
.upload-empty { padding: 30px; }
</style>
