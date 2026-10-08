export interface Menu {
  id: string
  name: string
  title: string
  parentId: string
  type: number
  path: string
  queryParam: string
  component: string
  keepAlive: 0 | 1
  sort: number
  code: string
  visible: 0 | 1
  status: 0 | 1
  icon: string
  redirect: string
  remark: string
}

export interface MenuItem extends Menu {
  children?: MenuItem[]
}
import type { DictData } from '@/components/dict/useDict'

/** 菜单类型。 */
export enum MenuType {
  DIRECTORY = 1,
  MENU = 2,
  BUTTON = 3,
  EXTERNAL = 4,
}

/** 菜单编辑表单接收的初始数据与辅助选项。 */
export interface MenuFormProps {
  initialModel?: Menu
  extendedData?: {
    parentId?: string
    menuTypeDict: DictData[]
    sysShowStatusDict: DictData[]
    sysRunStatusDict: DictData[]
    sysMenuKeepAliveStatus: DictData[]
    menuData: MenuItem[]
  }
}
