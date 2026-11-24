<script setup lang="ts">
import { useNoticeForm } from './hooks/useNoticeForm'
import RichTextEditor from '@/components/richTextEditor/RichTextEditor.vue'
import type { NoticeDetail } from './hooks/types'

const props = defineProps<{ initialModel?: NoticeDetail }>()
const { formRef, formModel, types, statuses, busy, noticeRules, imageHandlers } = useNoticeForm(props)
defineExpose({ formRef, formModel })
</script>

<template>
  <n-form ref="formRef" :model="formModel" :rules="noticeRules" label-placement="top" require-mark-placement="left">
    <n-form-item label="标题" path="title">
      <n-input v-model:value="formModel.title" :maxlength="100" placeholder="请输入标题" show-count />
    </n-form-item>
    <div class="publish-options">
      <n-form-item label="类型" path="noticeType">
        <n-radio-group v-model:value="formModel.noticeType" aria-label="类型">
          <n-space>
            <n-radio v-for="item in types" :key="item.value" :value="item.value" :label="item.label" />
          </n-space>
        </n-radio-group>
      </n-form-item>
      <n-form-item label="状态" path="status">
        <n-radio-group v-model:value="formModel.status" aria-label="状态">
          <n-space>
            <n-radio v-for="item in statuses" :key="item.value" :value="item.value" :label="item.label" />
          </n-space>
        </n-radio-group>
      </n-form-item>
      <n-form-item class="publish-time" label="发布时间" path="publishTime">
        <n-date-picker v-model:formatted-value="formModel.publishTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
          format="yyyy-MM-dd HH:mm:ss" clearable :placeholder="formModel.status === 0 ? '保存时立即发布' : '选择发布时间（可选）'" />
      </n-form-item>
    </div>
    <n-form-item label="正文" path="contentHtml" required>
      <RichTextEditor :key="initialModel?.id" v-model="formModel.contentHtml" v-model:busy="busy"
        aria-label="公告正文" placeholder="输入公告正文，或插入图片…" :maxlength="10000" :image-handlers="imageHandlers" />
    </n-form-item>
  </n-form>
</template>

<style scoped>
.publish-options { display: grid; grid-template-columns: 1fr 1fr minmax(240px, 1.5fr); gap: 0 20px; }
.publish-time :deep(.n-date-picker) { width: 100%; }
@media (max-width: 700px) {
  .publish-options { grid-template-columns: 1fr 1fr; column-gap: 8px; }
  .publish-time { grid-column: 1 / -1; }
}
</style>
