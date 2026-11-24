import { ref, watch, type Ref } from 'vue'
import { createRichImageSession, sanitizeRichHtml } from '@/components/richTextEditor/richTextHtml'
import { fetchNoticeImage } from './api'
import type { NoticeDetail } from './types'

/** 按公告可见状态读取图片，关闭弹窗或切换公告时释放上一份预览。 */
export function useNoticeDetailImages(show: Ref<boolean>, noticeSource: () => NoticeDetail | null) {
  const safeHtml = ref('')
  const imageError = ref(false)
  const attempt = ref(0)
  watch(
    [show, noticeSource, attempt],
    async ([visible, notice], _, onCleanup) => {
      imageError.value = false
      safeHtml.value = visible && notice?.contentHtml ? sanitizeRichHtml(notice.contentHtml) : ''
      if (!notice || !safeHtml.value.includes('<img')) return
      const images = createRichImageSession()
      onCleanup(images.dispose)
      const html = safeHtml.value
      const complete = await images.load(html, (fileId, signal) =>
        fetchNoticeImage({ noticeId: notice.id, fileId }, signal),
      )
      if (!images.signal.aborted) {
        safeHtml.value = images.display(html)
        imageError.value = !complete
      }
    },
    { immediate: true },
  )
  return { safeHtml, imageError, attempt }
}
