import { onMounted, ref } from 'vue'
import { fetchDictDataListByDictType } from '@/views/system/dictData/hooks/api'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'

/** 按类型加载真实字典选项，状态归属于当前页面实例。 */
export function useDict(type: string) {
  const dictData = ref<DictDataResult[]>([])
  onMounted(async () => {
    try {
      const response = await fetchDictDataListByDictType(type)
      if (response.code === 200) dictData.value = response.data
    } catch {
      // 请求错误由公共拦截器提示，保留空选项供用户重新进入页面加载。
    }
  })

  /** 查询值对应的展示信息，缺失时显示原始值。 */
  const getDictData = (value: string) =>
    dictData.value.find((item) => item.value === value) || {
      label: value,
      listClass: 'default' as const,
    }

  return { dictData, getDictData }
}
