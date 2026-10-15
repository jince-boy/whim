import menuData from './data/menus.json'
import dictTypeData from './data/dictTypes.json'
import dictData from './data/dictData.json'
import loginLogData from './data/loginLogs.json'
import operLogData from './data/operLogs.json'
import type { MenuItem, MenuType } from '@/views/system/menu/hooks/types'
import type { DictTypeResult } from '@/views/system/dictType/hooks/types'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'
import type { LoginLog } from '@/views/system/loginLog/hooks/types'
import type { OperLog } from '@/views/system/operLog/hooks/types'

/** 本地模拟数据库，复用各功能的接口数据类型。 */
export interface MockDatabase {
  menus: MenuItem[]
  dictTypes: DictTypeResult[]
  dictData: DictDataResult[]
  loginLogs: LoginLog[]
  operLogs: OperLog[]
}

export const mockStorageKey = 'whim:mock:database:v3'

/** 将演示种子转换为后端字段及绝对路径，不向业务代码暴露旧模型。 */
function initialMenus(): MenuItem[] {
  const rows = new Map(menuData.map((node) => [node.id, node]))
  /** 补齐旧种子的相对路径，外链继续使用有效URL。 */
  const pathOf = (id: string): string => {
    const node = rows.get(id)!
    if (node.path.startsWith('/')) return node.path
    return `${node.parentId !== '0' ? pathOf(node.parentId) : ''}/${node.path}`
  }
  return menuData.map((node) => ({
    id: node.id,
    parentId: node.parentId,
    menuName: node.title,
    menuType: node.type as MenuType,
    routeName: node.type === 3 || node.type === 4 ? null : node.name,
    path: node.type === 3 ? null : node.type === 4 ? node.redirect : pathOf(node.id),
    component: node.type === 2 ? node.component : null,
    param: node.queryParam || null,
    perms: node.type === 2 || node.type === 3 ? node.code || `system:${node.path}:list` : null,
    dataPermission: 0,
    isCache: node.keepAlive as 0 | 1,
    sort: node.sort,
    icon: node.icon,
    visible: node.visible as 0 | 1,
    status: node.status as 0 | 1,
    remark: node.remark,
    builtIn: node.component === 'system/menu/index' || node.parentId === '0' ? 1 : 0,
    createTime: node.createTime,
  }))
}

/** 优先恢复当前标签页的数据；损坏的缓存回退到 JSON 初始数据。 */
function loadDatabase(): MockDatabase {
  const initialData: MockDatabase = {
    menus: initialMenus(),
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
