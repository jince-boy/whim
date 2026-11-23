<script setup lang="ts">
import { computed, useId } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import { useDictStore } from '@/stores/modules/dict'
import { useNoticeDetailImages } from '../hooks/useNoticeDetailImages'
import '@/components/richEditor/richText.css'
import type { NoticeDetail } from '../hooks/types'

const props = defineProps<{ notice: NoticeDetail | null; loading?: boolean; backLabel?: string }>()
const show = defineModel<boolean>('show', { required: true })
const dictionary = useDictStore()
const titleId = useId()
const category = computed(() => props.notice && dictionary.getDictData('sys_notice_type', props.notice.noticeType))
const { safeHtml, imageError, attempt } = useNoticeDetailImages(show, () => props.notice)
</script>

<template>
  <n-modal v-model:show="show">
    <n-card class="notice-card" size="small" :bordered="false" closable :segmented="{ content: true }"
      role="dialog" aria-modal="true" :aria-labelledby="notice ? titleId : undefined" :aria-label="notice ? undefined : '消息详情'"
      :header-style="{ padding: '10px var(--reader-gutter)', backgroundColor: 'var(--n-action-color)' }"
      :content-style="{ padding: '18px var(--reader-gutter) 22px', minHeight: 0, overflowY: 'auto' }"
      @close="show = false">
      <template #header>
        <n-flex class="reader-tools" align="center" :wrap="false" :size="10">
          <template v-if="backLabel">
            <n-button class="back-button" quaternary size="small" @click="show = false">
              <template #icon><Icon name="arrow-left" :size="15" /></template>
              {{ backLabel }}
            </n-button>
            <span class="tool-separator" aria-hidden="true"></span>
          </template>
          <n-tag v-if="notice && category" :type="category.listClass" :bordered="false" size="small">
            <template #icon><Icon :name="notice.noticeType === 2 ? 'megaphone' : 'mail'" :size="14" /></template>
            {{ category.label }}
          </n-tag>
          <n-text v-else depth="2">消息详情</n-text>
        </n-flex>
      </template>

      <article v-if="notice" class="notice-article">
        <header class="article-heading">
          <h2 :id="titleId">{{ notice.title }}</h2>
          <n-text class="publication" depth="3">
            <Icon name="clock" :size="13" />
            <time v-if="notice.publishTime" :datetime="notice.publishTime.replace(' ', 'T')"
              :title="'发布时间 ' + notice.publishTime" :aria-label="'发布时间 ' + notice.publishTime">
              {{ notice.publishTime.slice(0, 16) }}
            </time>
            <span v-else>尚未发布</span>
          </n-text>
        </header>
        <div v-if="safeHtml" class="notice-body whim-rich-text" v-html="safeHtml"></div>
        <div v-else class="notice-body plain">{{ notice.content }}</div>
        <n-flex v-if="imageError" align="center" :size="8" class="image-error">
          <n-text depth="3">部分图片加载失败</n-text>
          <n-button text type="primary" @click="attempt++">重新加载</n-button>
        </n-flex>
      </article>
      <div v-else class="reader-state" role="status">
        <n-spin v-if="loading" size="small" aria-label="正在读取正文" />
        <n-empty v-else description="这条消息已不可用" />
      </div>
    </n-card>
  </n-modal>
</template>

<style scoped>
.notice-card.n-card {
  --reader-gutter: clamp(16px, 3vw, 24px);
  width: min(640px, calc(100vw - 24px));
  max-height: min(84dvh, 820px);
  border-radius: 10px;
  overflow: hidden;
}
.reader-tools { min-height: 28px; font-size: 13px; line-height: 1; }
.back-button { margin-inline-start: -8px; }
.reader-tools :deep(svg), .publication :deep(svg) { display: block; }
.tool-separator { width: 1px; height: 14px; background: var(--n-border-color); }
.notice-article { min-width: 0; overflow-wrap: anywhere; }
.article-heading { display: flex; align-items: baseline; flex-wrap: wrap; gap: 4px 16px; }
.article-heading h2 {
  flex: 1 1 16em;
  margin: 0;
  color: var(--n-title-text-color);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
}
.publication { display: inline-flex; align-items: center; gap: 5px; font-size: 12px; line-height: 1.5; white-space: nowrap; }
.notice-body { margin-top: 12px; font-size: 14px; line-height: 1.8; }
.notice-body > :deep(:first-child) { margin-block-start: 0; }
.notice-body > :deep(:last-child) { margin-block-end: 0; }
.notice-body :deep(img) { max-width: 100%; height: auto; border-radius: 4px; }
.notice-body :deep(a) { color: var(--primary-color); }
.plain { white-space: pre-wrap; }
.image-error { margin-top: 12px; }
.reader-state { min-height: 100px; display: grid; place-items: center; }
</style>
