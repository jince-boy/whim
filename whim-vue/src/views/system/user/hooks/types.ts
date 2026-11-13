import type { PostItem } from '@/views/system/post/hooks/types'
import type { OrganizationStatus } from '@/views/system/dept/hooks/types'

export type UserId = string | number

export interface UserAvatarQuery {
  id: string
  preview?: boolean
}

export interface UserItem {
  id: UserId
  username: string
  name: string
  avatar: string | null
  deptId: UserId | null
  deptName: string | null
  email: string | null
  phone: string | null
  gender: number
  status: OrganizationStatus
  remark: string | null
  createTime: string | null
  superAdministrator: boolean
}

export interface UserDetail extends UserItem {
  posts: PostItem[]
}

export interface UserQuery {
  username: string
  name: string
  phone: string
  status: OrganizationStatus | null
  deptId: string | null
  unassigned: boolean | null
}

export interface UserFormModel {
  id?: UserId
  username: string
  password: string
  confirmPassword: string
  name: string
  avatar: string | null
  avatarFile: File | null
  deptId: string | null
  email: string | null
  phone: string | null
  gender: number
  status: OrganizationStatus
  postIds: string[]
  remark: string | null
}

export interface UserFormProps {
  initialModel?: UserDetail
  defaultDeptId?: string | null
}

export interface PasswordFormModel {
  id: UserId
  password: string
  confirmPassword: string
}

export interface PasswordFormProps {
  user: UserItem
}

export interface UserRoleOption {
  id: UserId
  roleName: string
  roleCode: string
  status: OrganizationStatus
  disabled: boolean
}

export interface UserRoles {
  id: UserId
  roleIds: UserId[]
  options: UserRoleOption[]
}

export interface RoleFormProps {
  initialModel: UserRoles
}

export interface RoleFormModel {
  id: UserId
  roleIds: string[]
}

