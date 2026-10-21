/** 与后端共用权限表保持一致，ID 不强转为 Number。 */
export type MenuId = string | number

/** 菜单查询字段。 */
export interface MenuQuery {
  menuName: string
}
export enum MenuType {
  DIRECTORY = 1,
  MENU = 2,
  BUTTON = 3,
  EXTERNAL = 4,
}
export const menuTypeOptions = [
  { label: '目录', value: MenuType.DIRECTORY },
  { label: '菜单', value: MenuType.MENU },
  { label: '按钮', value: MenuType.BUTTON },
  { label: '外链', value: MenuType.EXTERNAL },
]
export interface MenuSave {
  parentId: MenuId
  menuName: string
  menuType: MenuType
  routeName: string | null
  path: string | null
  component: string | null
  param: string | null
  perms: string | null
  isCache: 0 | 1
  sort: number
  icon: string | null
  visible: 0 | 1
  status: 0 | 1
  remark: string | null
}
export interface MenuItem extends MenuSave {
  id: MenuId
  builtIn: 0 | 1
  protectedNode: boolean
  createTime?: string
  children?: MenuItem[]
}
export interface MenuFormProps {
  initialModel?: MenuSave & { id?: MenuId; builtIn?: 0 | 1 }
  extendedData?: { parentId?: MenuId; menuData: MenuItem[] }
}
