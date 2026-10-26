/** 菜单 ID 保留后端精度，不转换为 Number。 */
export type MenuId = string | number

/** 后端菜单节点类型。 */
export enum MenuType {
  DIRECTORY = 1,
  MENU = 2,
  BUTTON = 3,
  EXTERNAL = 4,
}

/** 当前用户菜单接口中的导航字段，独立于菜单管理页面。 */
export interface UserMenu {
  id: MenuId
  parentId: MenuId
  menuName: string
  menuType: MenuType
  routeName: string | null
  path: string | null
  component: string | null
  param: string | null
  isCache: 0 | 1
  sort: number
  icon: string | null
  visible: 0 | 1
  status: 0 | 1
  children?: UserMenu[]
}
