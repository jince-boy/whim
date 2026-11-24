import { computed, onScopeDispose, ref, useTemplateRef } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { fetchHomeTargets } from './api'
import type { HomePageRow, HomePageSave, HomeTarget } from './types'

/** 为当前弹窗创建独立草稿，按关键词分页搜索关联对象。 */
export function useHomePageForm(row?: HomePageRow) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<HomePageSave>({
    id: row?.id,
    targetType: row?.targetType ?? 4,
    targetId: row && row.targetType !== 4 ? String(row.targetId) : null,
    path: row?.path ?? '',
    component: row?.component ?? '',
    priority: row?.priority ?? 0,
    status: row?.status ?? 0,
  })
  const targets = ref<HomeTarget[]>([])
  const loading = ref(false)
  let keyword = ''
  let nextPage: number | undefined
  let version = 0
  const targetOptions = computed(() => {
    const options = targets.value.map((target) => ({
      value: String(target.id),
      label: target.name,
    }))
    if (
      row &&
      row.targetType === formModel.value.targetType &&
      row.targetId != null &&
      !options.some((option) => option.value === String(row.targetId))
    ) {
      options.unshift({
        value: String(row.targetId),
        label: row.targetName ?? String(row.targetId),
      })
    }
    return options
  })

  async function loadTargets(search: string, pageNum = 1) {
    if (formModel.value.targetType === 4) return
    const current = ++version
    if (pageNum === 1) {
      targets.value = []
      nextPage = undefined
    }
    keyword = search
    loading.value = true
    try {
      const response = await fetchHomeTargets({
        targetType: formModel.value.targetType,
        keyword: search,
        pageNum,
        pageSize: 30,
      })
      if (current !== version || response.code !== 200) return
      targets.value.push(...response.data.data)
      nextPage = pageNum < response.data.pages ? pageNum + 1 : undefined
    } catch {
      // 请求层展示错误，重新展开或搜索即可重试。
    } finally {
      if (current === version) loading.value = false
    }
  }

  function changeTargetType(type: number) {
    version++
    formModel.value.targetType = type
    formModel.value.targetId = null
    targets.value = []
    loading.value = false
    nextPage = undefined
    formRef.value?.restoreValidation()
  }

  function scrollTargets(event: Event) {
    const list = event.currentTarget as HTMLElement
    if (nextPage && !loading.value && list.scrollHeight - list.scrollTop - list.clientHeight < 32)
      void loadTargets(keyword, nextPage)
  }

  onScopeDispose(() => version++)
  return {
    formRef,
    formModel,
    targetOptions,
    loading,
    typeOptions: computed(() => dictionary.getDictNumberOptions('sys_home_target_type')),
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    changeTargetType,
    loadTargets,
    scrollTargets,
  }
}
