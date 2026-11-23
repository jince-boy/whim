import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { plainTextToHtml } from '@/components/richEditor/richText'
import { createNoticeRules } from './rule'
import { fetchNoticeImage, uploadNoticeImage } from './api'
import type { NoticeDetail, NoticeForm } from './types'

/** 只复制可编辑字段；版本与主键由打开弹窗时的详情确定。 */
export function useNoticeForm(props: { initialModel?: NoticeDetail }) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  function createDraft(notice?: NoticeDetail): NoticeForm {
    return {
      title: notice?.title ?? '',
      contentHtml: notice?.contentHtml ?? plainTextToHtml(notice?.content ?? ''),
      noticeType: notice?.noticeType ?? 1,
      status: notice?.status ?? 0,
      publishTime: notice?.publishTime ?? null,
    }
  }
  const formModel = ref(createDraft(props.initialModel))
  watch(() => props.initialModel, (notice) => {
    formModel.value = createDraft(notice)
    formRef.value?.restoreValidation()
  })
  const busy = ref(true)
  const noticeRules = createNoticeRules(() => busy.value)
  function loadImage(fileId: string, signal: AbortSignal) {
    if (!props.initialModel) throw new Error('公告尚未保存')
    return fetchNoticeImage({ noticeId: props.initialModel.id, fileId }, signal)
  }
  const types = computed(() => dictionary.getDictNumberOptions('sys_notice_type')
    .filter(option => option.value === 1 || option.value === 2))
  const statuses = computed(() => dictionary.getDictNumberOptions('sys_notice_status'))
  return { formRef, formModel, types, statuses, busy, noticeRules, loadImage, uploadNoticeImage }
}
