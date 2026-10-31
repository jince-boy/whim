import { MenuType, type MenuId, type UserMenu } from '@/stores/modules/permission/types'

export { MenuType, type MenuId } from '@/stores/modules/permission/types'

/** 菜单查询字段。 */
export interface MenuQuery {
  menuName: string
  menuType: MenuType | null
  status: 0 | 1 | null
}
/** 菜单字段沿用后端数值编码，展示文本由系统字典提供。 */
export interface MenuDictionaryOptions {
  menuTypes: { label: string; value: number }[]
  statuses: { label: string; value: number }[]
  visibilities: { label: string; value: number }[]
  cacheModes: { label: string; value: number }[]
}
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
  initialModel?: MenuItem
  parentMenu?: MenuItem
  menus: MenuItem[]
  dictionaryOptions: MenuDictionaryOptions
}
