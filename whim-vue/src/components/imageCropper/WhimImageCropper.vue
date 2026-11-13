<script setup lang="ts">
import { NAlert, NButton, NFlex, NModal, NSpace, NSpin, NText } from 'naive-ui'
import Icon from '@/components/icon/Icon.vue'
import type { ImageCropperProps } from './types'
import { useImageCropper } from './hooks/useImageCropper'

defineOptions({ name: 'WhimImageCropper', inheritAttrs: false })
const props = withDefaults(defineProps<ImageCropperProps>(), {
  title: '裁剪图片', outputWidth: 512, outputHeight: 512, outputType: 'image/png', quality: 0.92,
})
const show = defineModel<boolean>('show', { default: false })
const emit = defineEmits<{ confirm: [file: File] }>()
const { modalTarget, loading, saving, ready, error, initializeCropper, clearCropper, resetCropper, zoomImage, moveImageByKeyboard, confirmCrop } =
  useImageCropper(props, show, (file) => emit('confirm', file))
</script>

<template>
  <NModal v-bind="$attrs" v-model:show="show" :to="modalTarget" preset="dialog" :title="props.title" :show-icon="false"
    positive-text="使用图片" negative-text="取消" :loading="saving"
    :positive-button-props="{ disabled: !ready || saving }" :negative-button-props="{ disabled: saving }"
    :mask-closable="!saving" :close-on-esc="!saving" :closable="!saving"
    :style="{ width: '600px', maxWidth: 'calc(100vw - 32px)', maxHeight: 'calc(100dvh - 32px)', overflowY: 'auto' }"
    @after-enter="initializeCropper" @after-leave="clearCropper" @positive-click="confirmCrop">
    <NAlert v-if="error" type="error" class="image-cropper__error">{{ error }}</NAlert>
    <NSpin :show="loading">
      <div ref="cropperContainer" class="image-cropper__stage" role="group" aria-label="图片裁剪区域，方向键移动图片，Shift加速" :tabindex="ready && !saving ? 0 : -1" :aria-busy="loading || saving" @keydown="moveImageByKeyboard" />
    </NSpin>
    <NFlex align="center" justify="space-between" :size="8" class="image-cropper__toolbar">
      <NText depth="3">拖动或方向键移动图片，滚轮缩放</NText>
      <NSpace :size="4">
        <NButton size="small" quaternary :disabled="!ready || saving" aria-label="缩小图片" @click="zoomImage(-0.1)">
          <template #icon><Icon name="zoom-out" /></template>
        </NButton>
        <NButton size="small" quaternary :disabled="!ready || saving" aria-label="放大图片" @click="zoomImage(0.1)">
          <template #icon><Icon name="zoom-in" /></template>
        </NButton>
        <NButton size="small" :disabled="!ready || saving" @click="resetCropper">重置</NButton>
      </NSpace>
    </NFlex>
    <NText v-if="props.file?.type === 'image/gif'" depth="3" class="image-cropper__gif-hint">动图裁剪后将保存为静态图片</NText>
  </NModal>
</template>

<style scoped>
.image-cropper__error { margin-bottom: 12px; }
.image-cropper__stage { height: min(360px, 48dvh); min-height: 180px; }
.image-cropper__stage :deep(cropper-canvas) { height: 100%; }
.image-cropper__toolbar { margin-top: 12px; font-size: 12px; }
.image-cropper__gif-hint { display: block; margin-top: 8px; font-size: 12px; }
</style>
