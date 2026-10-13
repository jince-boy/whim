import type { MenuItem, MenuSave } from '@/views/system/menu/hooks/types'
import { database, persistDatabase } from './database'
import { currentTime, fail, findById, success, type MockParams } from './utils'

/** 平铺目录转为排序树，搜索结果保留祖先。 */
export function buildMenuTree(rows: MenuItem[], name = ''): MenuItem[] {
  const included = new Set<string>()
  const index = new Map(rows.map((row) => [String(row.id), row]))
  for (const row of rows) {
    if (!row.menuName.includes(name.trim())) continue
    let current: MenuItem | undefined = row
    while (current && !included.has(String(current.id))) {
      included.add(String(current.id))
      current = index.get(String(current.parentId))
    }
  }
  const nodes = new Map<string, MenuItem>()
  for (const row of rows
    .filter((item) => included.has(String(item.id)))
    .sort((a, b) => a.sort - b.sort))
    nodes.set(String(row.id), { ...row, children: [] })
  const roots: MenuItem[] = []
  for (const node of nodes.values()) {
    const parent = nodes.get(String(node.parentId))
    if (parent) parent.children!.push(node)
    else roots.push(node)
  }
  return roots
}

/** 演示接口使用与后端相同的路径和字段，不影响真实模式的网络请求。 */
export function handleMenu(path: string, method: string, params: MockParams, data: MockParams) {
  if (path === '/system/menu/userMenus' && method === 'GET') {
    const active = database.menus.filter((node) => node.status === 0 && node.menuType !== 3)
    const index = new Map(active.map((node) => [String(node.id), node]))
    const visible = active.filter((node) => {
      let current: MenuItem | undefined = node
      const visited = new Set<string>()
      while (current && String(current.parentId) !== '0') {
        if (visited.has(String(current.id))) return false
        visited.add(String(current.id))
        current = index.get(String(current.parentId))
      }
      return !!current
    })
    return success(buildMenuTree(visible))
  }
  if (path === '/system/menu' && method === 'GET')
    return success(buildMenuTree(database.menus, String(params.menuName || '')))
  const match = /^\/system\/menu\/([^/]+)$/.exec(path)
  const existing = match ? findById(database.menus, match[1]) : undefined
  if (match && method === 'GET') return success(existing)
  if (match && method === 'DELETE') {
    if (existing!.builtIn) fail('内置节点不能删除')
    if (database.menus.some((node) => String(node.parentId) === String(existing!.id)))
      fail('请先删除子节点')
    database.menus = database.menus.filter((node) => node.id !== existing!.id)
    persistDatabase()
    return success(null, '菜单删除成功')
  }
  if (!(path === '/system/menu' && method === 'POST') && !(match && method === 'PUT')) return
  const model = data as unknown as MenuSave
  if (!model.menuName?.trim()) fail('菜单名称不能为空')
  const ancestors = new Set<string>(existing ? [String(existing.id)] : [])
  let parent = String(model.parentId) === '0' ? undefined : findById(database.menus, model.parentId)
  if (model.menuType === 3 && parent?.menuType !== 2) fail('按钮权限必须属于页面菜单')
  if (model.menuType !== 3 && parent && parent.menuType !== 1) fail('页面和目录只能属于目录')
  while (parent) {
    if (ancestors.has(String(parent.id))) fail('不能移动到自身或后代节点下')
    ancestors.add(String(parent.id))
    parent = String(parent.parentId) === '0' ? undefined : findById(database.menus, parent.parentId)
  }
  if (
    existing?.builtIn &&
    (model.menuType !== existing.menuType ||
      model.perms !== existing.perms ||
      model.dataPermission !== existing.dataPermission)
  )
    fail('内置权限语义不能修改')
  if (
    database.menus.some(
      (node) =>
        node.id !== existing?.id &&
        ((model.routeName && model.routeName === node.routeName) ||
          (model.perms && model.perms === node.perms) ||
          ([1, 2].includes(model.menuType) &&
            [1, 2].includes(node.menuType) &&
            model.path === node.path)),
    )
  )
    fail('路由或权限标识已存在')
  const menu: MenuItem = {
    ...model,
    id: existing?.id || crypto.randomUUID(),
    builtIn: existing?.builtIn || 0,
    createTime: existing?.createTime || currentTime(),
  }
  if (existing) Object.assign(existing, menu)
  else database.menus.push(menu)
  persistDatabase()
  return success(existing ? null : menu.id, existing ? '菜单修改成功' : '菜单创建成功')
}
