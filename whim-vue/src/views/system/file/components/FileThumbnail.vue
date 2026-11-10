<script setup lang="ts">
import { NButton, NImage, NImagePreview, NSkeleton, NText } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import { useFileThumbnail } from '../hooks/useFileThumbnail'
import type { FileThumbnailProps } from '../hooks/types'

const props = defineProps<FileThumbnailProps>()
const {
  imageUrl, loading, unavailable, isImage, handleImageError,
  previewUrl, previewShown, previewLoading, openPreview,
} = useFileThumbnail(props)
</script>

<template>
  <span ref="thumbnailElement" class="file-thumbnail">
    <n-button v-if="imageUrl && !unavailable" text size="small" class="file-thumbnail-trigger"
      :loading="previewLoading" :disabled="!props.canPreview" :aria-label="`查看图片：${props.file.originalName}`"
      title="查看原图" @click="openPreview">
      <n-image :src="imageUrl" :alt="props.file.originalName" :width="32" :height="32"
        object-fit="contain" preview-disabled @error="handleImageError" />
    </n-button>
    <n-skeleton v-else-if="loading" :width="32" :height="32" />
    <n-text v-else depth="3" :title="isImage ? '暂无图片预览' : undefined">
      <Icon :name="isImage ? 'image' : 'file'" :size="22" />
    </n-text>
    <n-image-preview v-model:show="previewShown" :src="previewUrl" show-toolbar-tooltip />
  </span>
</template>

<style scoped>
.file-thumbnail {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  overflow: hidden;
  border-radius: 4px;
}

.file-thumbnail-trigger {
  width: 32px;
  height: 32px;
}
</style>
