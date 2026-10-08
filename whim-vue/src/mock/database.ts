import menuData from './data/menus.json'
import dictTypeData from './data/dictTypes.json'
import dictData from './data/dictData.json'
import loginLogData from './data/loginLogs.json'
import operLogData from './data/operLogs.json'
import type { Menu } from '@/views/system/menu/hooks/types'
import type { DictTypeResult } from '@/views/system/dictType/hooks/types'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'
import type { LoginLog } from '@/views/system/loginLog/hooks/types'
import type { OperLog } from '@/views/system/operLog/hooks/types'

/** 本地模拟数据库，复用各功能的接口数据类型。 */
export interface MockDatabase {
  menus: (Menu & { createTime: string })[]
  dictTypes: DictTypeResult[]
  dictData: DictDataResult[]
  loginLogs: LoginLog[]
  operLogs: OperLog[]
}

export const mockStorageKey = 'whim:mock:database:v1'

/** 优先恢复当前标签页的数据；损坏的缓存回退到 JSON 初始数据。 */
function loadDatabase(): MockDatabase {
  const initialData: MockDatabase = {
    menus: menuData as MockDatabase['menus'],
    dictTypes: dictTypeData,
    dictData: dictData as DictDataResult[],
    loginLogs: loginLogData,
    operLogs: operLogData,
  }
  try {
    const cached = sessionStorage.getItem(mockStorageKey)
    if (cached) {
      const parsed = JSON.parse(cached) as MockDatabase
      if (
        Object.keys(initialData).every((key) => Array.isArray(parsed[key as keyof MockDatabase]))
      ) {
        return parsed
      }
    }
  } catch {
    console.warn('本地测试数据读取失败，已恢复 JSON 初始数据')
  }
  return structuredClone(initialData)
}

// 模拟服务端共享数据；页面只通过接口访问，响应会复制以隔离表单草稿。
export const database = loadDatabase()

/** 保存模拟写入，刷新页面后保留当前标签页的操作结果。 */
export function persistDatabase(): void {
  try {
    sessionStorage.setItem(mockStorageKey, JSON.stringify(database))
  } catch {
    console.warn('本地测试数据保存失败，本次修改仅在当前页面内生效')
  }
}
