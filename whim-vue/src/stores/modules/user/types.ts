/** 当前登录用户的信息及授权结果。 */
export interface UserInfoResult {
  userId: string | number
  username: string
  name: string | null
  avatar: string | null
  deptId: string | number | null
  loginType: string
  roleCodeSet: string[]
  permissionCodeSet: string[]
  roleInfoList: RoleInfo[] | null
}

/** 当前登录用户的角色摘要。 */
export interface RoleInfo {
  roleId: string | number
  roleName: string
  roleCode: string
  dataScope: number | null
}

/** 当前登录用户的共享状态，菜单由导航store独立维护。 */
export interface UserState {
  user: UserInfoResult | null
}
