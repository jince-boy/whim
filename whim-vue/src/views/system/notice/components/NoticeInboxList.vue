<script setup lang="ts">
import { useThemeVars } from 'naive-ui'
import { computed } from 'vue'
import Icon from '@/components/icon/Icon.vue'
import { useDictStore } from '@/stores/modules/dict'
import type { NoticeId, NoticeItem } from '../hooks/types'

defineProps<{ category: number; label: string; items: NoticeItem[]; loading: boolean; error: boolean }>()
defineEmits<{ select: [category: number]; open: [id: NoticeId]; retry: [] }>()
const theme = useThemeVars()
const dictionary = useDictStore()
const categories = computed(() => dictionary.getDictNumberOptions('sys_notice_type')
  .filter(option => option.value === 1 || option.value === 2))
</script>

<template>
  <div class="inbox-list">
    <div class="categories" role="group" aria-label="消息分类">
      <button v-for="option in categories" :key="option.value" type="button" :aria-pressed="category === option.value"
        @click="$emit('select', option.value)">{{ option.label }}</button>
    </div>
    <div class="messages" :aria-busy="loading">
      <div v-if="error" class="empty" role="status">
        <span>消息加载失败</span><n-button size="small" @click="$emit('retry')">重新加载</n-button>
      </div>
      <div v-else-if="loading && !items.length" class="empty"><n-spin size="small" aria-label="正在加载消息" /></div>
      <ul v-else-if="items.length">
        <li v-for="item in items" :key="item.id">
          <button type="button" class="message" :class="{ unread: !item.readFlag }" @click="$emit('open', item.id)">
            <span class="mark" :class="{ announcement: item.noticeType === 2 }">
              <Icon :name="item.noticeType === 2 ? 'megaphone' : 'mail'" :size="18" />
            </span>
            <span class="summary">
              <span class="title">{{ item.title }}</span>
              <time :datetime="item.publishTime.replace(' ', 'T')">{{ item.publishTime.slice(0, 16) }}</time>
            </span>
            <span v-if="!item.readFlag" class="dot" role="img" aria-label="未读"></span>
            <Icon v-else name="chevron-right" :size="15" class="arrow" />
          </button>
        </li>
      </ul>
      <div v-else class="empty" role="status">
        <Icon :name="category === 1 ? 'mail-open' : 'megaphone'" :size="26" />
        <span>暂无{{ label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inbox-list { display: flex; flex-direction: column; min-height: 0; }
.categories { display: grid; grid-auto-flow: column; grid-auto-columns: 1fr; gap: 3px; padding: 3px; margin: 0 0 10px;
  border-radius: 9px; background: v-bind('theme.actionColor'); border: 1px solid v-bind('theme.dividerColor'); }
.categories button, .message { font: inherit; border: 0; background: transparent; cursor: pointer; color: v-bind('theme.textColor2'); }
.categories button { padding: 5px 0; border-radius: 6px; }
.categories button[aria-pressed="true"] { color: v-bind('theme.primaryColor'); background: v-bind('theme.cardColor');
  font-weight: 650; box-shadow: 0 1px 4px #0002; }
button:focus-visible { outline: 2px solid v-bind('theme.primaryColor'); outline-offset: -2px; }
.messages { overflow-y: auto; min-height: 0; }
ul { padding: 0; margin: 0; list-style: none; }
li + li { border-top: 1px solid v-bind('theme.dividerColor'); }
.message { width: 100%; display: flex; align-items: center; gap: 12px; padding: 12px 6px; border-radius: 6px; text-align: start; }
.message:hover { background: v-bind('theme.hoverColor'); }
.mark { display: grid; place-items: center; flex: 0 0 36px; height: 36px; border-radius: 10px;
  color: v-bind('theme.infoColor'); background: color-mix(in srgb, currentColor 11%, transparent); }
.mark.announcement { color: v-bind('theme.primaryColor'); }
.summary { display: grid; flex: 1; min-width: 0; gap: 3px; }
.title { overflow: hidden; overflow-wrap: anywhere; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5; }
.unread .title { color: v-bind('theme.textColor1'); font-weight: 650; }
time { color: v-bind('theme.textColor3'); font-size: 12px; font-variant-numeric: tabular-nums; }
.dot { flex: 0 0 7px; height: 7px; border-radius: 50%; background: v-bind('theme.primaryColor'); margin-inline: 4px; }
.arrow, .empty { color: v-bind('theme.textColor3'); }
.empty { min-height: 140px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; }
</style>
