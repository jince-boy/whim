<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'
import { useNoticeInbox } from '../hooks/useNoticeInbox'
import NoticeInboxList from './NoticeInboxList.vue'
import NoticeDetail from './NoticeDetail.vue'

const {
  box, history, detail, showBell, showHistory, showDetail, boxLoading, historyLoading, detailLoading,
  boxError, historyError, page, pageCount, category, categoryLabel, fromHistory,
  selectCategory, canClearSystemMessages, confirmClearSystemMessages, loadBox, loadHistory, openDetail, openHistory, toggleBell,
} = useNoticeInbox()
</script>

<template>
  <n-popover :show="showBell" trigger="click" placement="bottom-end" :show-arrow="false"
    style="width: min(380px, calc(100vw - 24px)); box-sizing: border-box; border-radius: 12px"
    @update:show="toggleBell">
    <template #trigger>
      <n-button quaternary native-focus-behavior size="small" style="width: 32px; height: 32px"
        :aria-label="box.unreadCount ? '消息中心，' + box.unreadCount + '条未读' : '消息中心'" @mousedown.prevent>
        <n-badge :value="box.unreadCount" :max="99"><Icon name="bell" :size="18" /></n-badge>
      </n-button>
    </template>
    <div class="box-heading">
      <h2>消息中心</h2><n-text depth="3">{{ box.unreadCount ? '共 ' + box.unreadCount + ' 条未读' : '全部已读' }}</n-text>
    </div>
    <NoticeInboxList class="recent" :category="category" :label="categoryLabel" :items="box.items" :loading="boxLoading" :error="boxError"
      @select="selectCategory" @open="openDetail" @retry="loadBox" />
    <template #footer>
      <n-button quaternary block size="small" @click="openHistory">查看全部{{ categoryLabel }} <Icon name="arrow-right" :size="15" class="more" /></n-button>
    </template>
  </n-popover>

  <n-modal v-model:show="showHistory" preset="card" title="全部消息" :bordered="false"
    :segmented="{ footer: true }" :footer-style="{ paddingBlock: '10px' }"
    style="width: min(660px, calc(100vw - 24px)); border-radius: 14px">
    <NoticeInboxList class="history" :category="category" :label="categoryLabel" :items="history" :loading="historyLoading" :error="historyError"
      @select="selectCategory" @open="openDetail" @retry="loadHistory(page)" />
    <template v-if="canClearSystemMessages || pageCount > 1" #footer>
      <n-flex align="center" :size="8">
        <n-button v-if="canClearSystemMessages" quaternary size="small" :disabled="historyLoading" @click="confirmClearSystemMessages">
          <template #icon><Icon name="trash-2" :size="14" /></template>
          清空系统消息
        </n-button>
        <n-pagination v-if="pageCount > 1" class="history-pages" :page="page" :page-count="pageCount"
          :page-slot="5" size="small" @update:page="loadHistory" />
      </n-flex>
    </template>
  </n-modal>
  <NoticeDetail v-model:show="showDetail" :notice="detail" :loading="detailLoading" :back-label="fromHistory ? '返回列表' : undefined" />
</template>

<style scoped>
.box-heading { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; padding: 9px 2px 14px; }
.box-heading h2 { margin: 0; font-size: 18px; line-height: 1.4; }
.box-heading > :last-child { font-size: 12px; }
.recent { max-height: min(440px, 60dvh); }
.history { max-height: min(620px, 65dvh); }
.history-pages { margin-inline-start: auto; }
.more { margin-inline-start: 6px; }
</style>
