import { request, RequestMethod } from '@/api'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'

/** 一次读取全部字典，供当前登录会话共享。 */
export function fetchDictionaryList() {
  return request<DictDataResult[]>('/system/dictData/list', RequestMethod.GET)
}
