import type { Menu, MenuItem } from '@/views/system/menu/hooks/types'
import { database, persistDatabase } from './database'
import { currentTime, fail, findById, readIds, success, type MockParams } from './utils'

/** 将平铺菜单转换为树；搜索时同时保留匹配节点的祖先。 */
export function buildMenuTree(rows: Menu[], title = ''): MenuItem[] {
  const included = new Set<string>()
  for (const row of rows) {
    if (!row.title.toLowerCase().includes(title.trim().toLowerCase())) continue
    let current: Menu | undefined = row
    while (current && !included.has(current.id)) {
      included.add(current.id)
      current = rows.find((item) => item.id === current!.parentId)
    }
  }
  const nodes = new Map<string, MenuItem>()
  for (const row of rows.filter((item) => included.has(item.id)).sort((a, b) => a.sort - b.sort)) {
    nodes.set(row.id, { ...row })
  }
  const roots: MenuItem[] = []
  for (const node of nodes.values()) {
    const parent = nodes.get(node.parentId)
    if (parent) (parent.children ??= []).push(node)
    else roots.push(node)
  }
  return roots
}

/** 模拟菜单查询与维护，并检查父节点及循环引用。 */
export function handleMenu(path: string, method: string, params: MockParams, data: MockParams) {
  if (path === '/system/menu/list' && method === 'GET') {
    return success(buildMenuTree(database.menus, String(params.title ?? '')))
  }
  if (path === '/system/menu/detail' && method === 'GET') {
    return success(findById(database.menus, params.id))
  }
  if (['/system/menu/add', '/system/menu/update'].includes(path)) {
    const inserting = path.endsWith('/add') && method === 'POST'
    if (!inserting && !(path.endsWith('/update') && method === 'PUT')) return
    const existing = inserting ? undefined : findById(database.menus, data.id)
    const menu = { ...existing, ...data } as Menu
    if (!menu.title?.trim()) fail('请输入菜单名称')
    const parentId = String(menu.parentId || '0')
    let parent = parentId === '0' ? undefined : findById(database.menus, parentId)
    const visited = new Set<string>(existing ? [existing.id] : [])
    while (parent) {
      if (visited.has(parent.id)) fail('不能将菜单移动到自己或子菜单下')
      visited.add(parent.id)
      parent = database.menus.find((item) => item.id === parent!.parentId)
    }
    if (inserting) {
      database.menus.push({ ...menu, id: crypto.randomUUID(), parentId, createTime: currentTime() })
    } else {
      Object.assign(existing!, menu, { parentId })
    }
    persistDatabase()
    return success(null)
  }
  if (path === '/system/menu/delete' && method === 'DELETE') {
    const ids = readIds(params.menuIds)
    if (database.menus.some((menu) => ids.includes(menu.parentId) && !ids.includes(menu.id))) {
      fail('请先删除子菜单')
    }
    database.menus = database.menus.filter((menu) => !ids.includes(menu.id))
    persistDatabase()
    return success(null)
  }
}
