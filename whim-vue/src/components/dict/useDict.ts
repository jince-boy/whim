import { fetchDictDataListByDictType } from '@/views/system/dictData/hooks/api'

type TagType = 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'

export interface DictData {
  id: number | string
  dictType: string
  label: string
  value: string
  listClass: TagType
  sort: number
  remark: string
}

export async function useDict(dictType: string) {
  const res = await fetchDictDataListByDictType(dictType)
  const dictData = res.code === 200 ? res.data : []
  const getDictData = (value: string) => {
    return dictData.find((item) => item.value === value)
  }
  return {
    dictData, // 普通数组
    getDictData,
  }
}
