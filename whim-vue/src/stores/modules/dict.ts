import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { useAuthStore } from './auth'
import { fetchDictionaryList } from './dict/api'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'

/** 登录或刷新浏览器后统一加载一次字典，页面共用内存缓存。 */
export const useDictStore = defineStore('dict', () => {
  const dictionaries = shallowRef(new Map<string, DictDataResult[]>())
  const initialized = ref(false)
  let revision = 0
  let pending: Promise<void> | undefined

  /** 根据类型读取字典选项，缺少该类型时返回空列表。 */
  function getDictOptions(type: string) {
    return dictionaries.value.get(type) ?? []
  }

  /** 将指定类型的字典转换为数字选项，保留后端排序。 */
  function getDictNumberOptions(type: string) {
    return getDictOptions(type)
      .map((item) => ({ label: item.label, value: Number(item.value) }))
      .filter((item) => Number.isFinite(item.value))
  }

  /** 根据类型和值查询展示信息，缺失时显示原始值。 */
  function getDictData(type: string, value: string | number) {
    return getDictOptions(type).find((item) => item.value === String(value)) ?? {
      label: String(value),
      listClass: 'default' as const,
    }
  }

  /** 初始化后直接复用缓存；并发加载合并为一次请求，旧会话响应不会回写。 */
  async function loadDictionaries(): Promise<void> {
    if (pending) return pending
    if (initialized.value) return
    const token = useAuthStore().getToken
    if (!token) return
    const version = revision
    const request = (async () => {
      const response = await fetchDictionaryList()
      if (version !== revision || token !== useAuthStore().getToken) return
      if (response.code !== 200) throw new Error(response.message || '字典加载失败，请重试')
      const grouped = new Map<string, DictDataResult[]>()
      for (const item of response.data) {
        const items = grouped.get(item.type)
        if (items) items.push(item)
        else grouped.set(item.type, [item])
      }
      dictionaries.value = grouped
      initialized.value = true
    })()
    pending = request
    try {
      await request
    } finally {
      if (pending === request) pending = undefined
    }
  }

  /** 清空字典并使未完成请求失效，用于退出登录、切换账号和主动刷新。 */
  function clearDictionaries(): void {
    revision++
    pending = undefined
    dictionaries.value = new Map()
    initialized.value = false
  }

  /** 字典维护成功后重新读取，所有使用字典的页面自动更新。 */
  async function refreshDictionaries(): Promise<void> {
    clearDictionaries()
    await loadDictionaries()
  }

  return {
    dictionaries,
    initialized,
    getDictOptions,
    getDictNumberOptions,
    getDictData,
    loadDictionaries,
    clearDictionaries,
    refreshDictionaries,
  }
})
