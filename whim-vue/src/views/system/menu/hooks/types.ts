import { MenuType, type MenuId, type UserMenu } from '@/stores/modules/permission/types'

export { MenuType, type MenuId } from '@/stores/modules/permission/types'

/** 菜单查询字段。 */
export interface MenuQuery {
  menuName: string
}
export const menuTypeOptions = [
  { label: '目录', value: MenuType.DIRECTORY },
  { label: '菜单', value: MenuType.MENU },
  { label: '按钮', value: MenuType.BUTTON },
  { label: '外链', value: MenuType.EXTERNAL },
]
export interface MenuSave extends Omit<UserMenu, 'id' | 'children'> {
  perms: string | null
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
