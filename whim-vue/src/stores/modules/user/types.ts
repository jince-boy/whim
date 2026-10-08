/** 当前登录用户的信息及授权结果。 */
export interface UserInfoResult {
  user: User | null
  permissionCode: string[]
  roleCode: string[]
  menus: MenuItem[]
}

export interface Role {
  id: string
  name: string
  code: string
  dataScope: number
  description: string
  status: number
  remark: string
}

export interface User {
  id: string
  deptId: number
  username: string
  avatar: string | null
  name: string
  email: string
  phone: string
  gender: number
  status: number
  remark: string
  roles: Role[]
}

export interface MenuItem {
  id: string
  name: string
  title: string
  path: string
  queryParam: string
  component: string
  icon: string
  keepAlive: 0 | 1
  visible: 0 | 1
  status: 0 | 1
  redirect: string
  remark: string
  children?: MenuItem[]
}
