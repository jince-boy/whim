import { computed, ref, shallowRef, watch } from 'vue'
import { subscribeSystemEvents } from '@/api'
import { useFormDialog } from '@/components/dialog'
import { useAuthStore } from '@/stores/modules/auth'
import { useDictStore } from '@/stores/modules/dict'
import { clearSystemMessages, fetchNoticeBox, fetchNoticeInbox, readNotice } from './api'
import type { NoticeBox, NoticeDetail, NoticeId, NoticeItem } from './types'

/** 管理当前标签页的铃铛、历史列表和阅读详情，切换账号时丢弃旧请求。 */
export function useNoticeInbox() {
  const auth = useAuthStore()
  const { openDeleteDialog } = useFormDialog()
  const dictionary = useDictStore()
  const category = ref(3)
  const categoryLabel = computed(() => {
    const label = dictionary.getDictData('sys_notice_type', category.value).label
    return category.value === 3 ? label + '消息' : label
  })
  const fromHistory = ref(false)
  const box = shallowRef<NoticeBox>({ unreadCount: 0, items: [] })
  const history = shallowRef<NoticeItem[]>([])
  const detail = shallowRef<NoticeDetail | null>(null)
  const showBell = ref(false)
  const showHistory = ref(false)
  const showDetail = ref(false)
  const boxLoading = ref(false)
  const historyLoading = ref(false)
  const detailLoading = ref(false)
  const boxError = ref(false)
  const historyError = ref(false)
  const page = ref(1)
  const pageCount = ref(1)
  const clearThroughId = computed(() => {
    if (!showHistory.value || category.value !== 3 || historyError.value || !history.value.length) return undefined
    // 历史页翻到后面时仍以最近消息为界，清空范围包含前面各页。
    return (page.value === 1 ? history.value[0] : box.value.items[0])?.id
  })
  const canClearSystemMessages = computed(() => clearThroughId.value !== undefined)
  let active: AbortController | undefined
  let boxRequest = 0
  let historyRequest = 0
  let detailRequest = 0
  let selectedId: NoticeId | undefined
  let publicationTimer: ReturnType<typeof setTimeout> | undefined
  let refreshTimer: ReturnType<typeof setTimeout> | undefined
  let detailChanged = false

  async function loadBox() {
    if (!active) return
    const signal = active.signal
    const current = ++boxRequest
    boxLoading.value = true
    boxError.value = false
    try {
      const { data } = await fetchNoticeBox({ noticeType: category.value }, signal)
      if (signal.aborted || current !== boxRequest) return
      box.value = data
      clearTimeout(publicationTimer)
      if (data.refreshAfterMillis != null) {
        // 使用服务端的相对时间，不依赖设备时钟。超长计划分段等待，避免浏览器定时器溢出。
        publicationTimer = setTimeout(
          refreshInbox,
          Math.min(2147000000, data.refreshAfterMillis) + 100,
        )
      }
    } catch {
      if (!signal.aborted && current === boxRequest) {
        boxError.value = true
        clearTimeout(publicationTimer)
        publicationTimer = setTimeout(refreshInbox, 30000)
      }
    } finally {
      if (!signal.aborted && current === boxRequest) boxLoading.value = false
    }
  }

  function refreshInbox() {
    void loadBox()
    if (showHistory.value) void loadHistory(page.value)
  }

  // 阅读响应与当前账号的已读推送常在同一时间到达，合并刷新保留未启用推送时的更新能力。
  function scheduleRefresh(contentChanged: boolean) {
    detailChanged ||= contentChanged
    if (refreshTimer !== undefined) return
    refreshTimer = setTimeout(() => {
      refreshTimer = undefined
      if (detailChanged && showDetail.value && selectedId !== undefined) void openDetail(selectedId)
      else refreshInbox()
      detailChanged = false
    }, 100)
  }

  function selectCategory(next: number) {
    if (next === category.value) return
    category.value = next
    box.value = { ...box.value, items: [] }
    // 分类切换时，已关闭列表的旧请求也可能仍在返回。
    historyRequest++
    history.value = []
    page.value = pageCount.value = 1
    refreshInbox()
  }

  async function loadHistory(nextPage: number) {
    if (!active) return
    const signal = active.signal
    const current = ++historyRequest
    page.value = nextPage
    historyLoading.value = true
    historyError.value = false
    try {
      const { data } = await fetchNoticeInbox(
        { pageNum: nextPage, pageSize: 10, noticeType: category.value },
        signal,
      )
      if (signal.aborted || current !== historyRequest) return
      pageCount.value = Math.max(1, Number(data.pages))
      if (nextPage > pageCount.value) {
        await loadHistory(pageCount.value)
        return
      }
      history.value = data.data
    } catch {
      if (!signal.aborted && current === historyRequest) historyError.value = true
    } finally {
      if (!signal.aborted && current === historyRequest) historyLoading.value = false
    }
  }

  async function openDetail(id: NoticeId) {
    if (!active) return
    const signal = active.signal
    const current = ++detailRequest
    selectedId = id
    if (!showDetail.value) fromHistory.value = showHistory.value
    showHistory.value = false
    showBell.value = false
    showDetail.value = true
    detailLoading.value = true
    detail.value = null
    try {
      const { data } = await readNotice({ id }, signal)
      if (signal.aborted || current !== detailRequest || !showDetail.value) return
      detail.value = data
    } catch {
      // 不保留已关闭、已删除或无权读取的正文。
    } finally {
      if (!signal.aborted && current === detailRequest) {
        detailLoading.value = false
        scheduleRefresh(false)
      }
    }
  }

  function confirmClearSystemMessages() {
    const throughId = clearThroughId.value
    if (!active || throughId === undefined) return
    const signal = active.signal
    // 打开弹窗时固定账号与清空边界，重试同一请求也会保留后来收到的消息。
    const { close } = openDeleteDialog({
      title: '清空系统消息',
      content: '将清空你已收到的所有系统消息，包括已读和未读消息。',
      modalProps: {
        type: 'error',
        positiveText: '确认清空',
        onAfterLeave: () => signal.removeEventListener('abort', close),
      },
      onSubmit: () => clearSystemMessages({ throughId }, signal),
      successMessage: '系统消息已清空',
      onSuccess: () => {
        if (signal.aborted) return
        if (category.value === 3) page.value = 1
        refreshInbox()
      },
    })
    signal.addEventListener('abort', close, { once: true })
  }

  function openHistory() {
    showBell.value = false
    showHistory.value = true
    void loadHistory(1)
  }
  function toggleBell(show: boolean) {
    showBell.value = show
    if (show) void loadBox()
  }

  watch(showDetail, (visible) => {
    if (!visible && fromHistory.value && active) {
      fromHistory.value = false
      showHistory.value = true
      void loadHistory(page.value)
    }
  })

  watch(
    () => auth.token,
    (token, _, onCleanup) => {
      category.value = 3
      fromHistory.value = false
      box.value = { unreadCount: 0, items: [] }
      history.value = []
      page.value = pageCount.value = 1
      detail.value = null
      selectedId = undefined
      showBell.value = showHistory.value = showDetail.value = false
      boxLoading.value = historyLoading.value = detailLoading.value = false
      boxError.value = historyError.value = false
      if (!token) return
      const controller = new AbortController()
      active = controller
      void loadBox()
      void subscribeSystemEvents(
        controller.signal,
        () => { if (!controller.signal.aborted) scheduleRefresh(true) },
        (name) => {
          if (name === 'systemMessagesCleared') {
            if (category.value === 3) page.value = 1
            scheduleRefresh(category.value === 3)
            return
          }
          if (name === 'noticeChanged' || name === 'noticeRead' || name === 'systemMessage')
            scheduleRefresh(name === 'noticeChanged')
        },
      )
      onCleanup(() => {
        controller.abort()
        clearTimeout(refreshTimer)
        refreshTimer = undefined
        detailChanged = false
        clearTimeout(publicationTimer)
        active = undefined
      })
    },
    { immediate: true, flush: 'sync' },
  )

  return {
    category,
    categoryLabel,
    fromHistory,
    selectCategory,
    canClearSystemMessages,
    confirmClearSystemMessages,
    box,
    history,
    detail,
    showBell,
    showHistory,
    showDetail,
    boxLoading,
    historyLoading,
    detailLoading,
    boxError,
    historyError,
    page,
    pageCount,
    loadBox,
    loadHistory,
    openDetail,
    openHistory,
    toggleBell,
  }
}
