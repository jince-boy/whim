import type { TreeSelectOption } from 'naive-ui'
import type { OrganizationStatus } from '@/views/system/dept/hooks/types'

export type PostId = string | number

export interface PostItem {
  id: PostId
  deptId: string | number | null
  postName: string
  postCode: string
  sort: number
  status: OrganizationStatus
  remark: string | null
  createTime: string | null
  updateTime: string | null
}

export interface PostSave {
  /** 修改时携带目标ID，新增时省略。 */
  id?: PostId
  deptId: string | null
  postName: string
  postCode: string
  sort: number | null
  status: OrganizationStatus
  remark: string | null
}

export interface PostQuery {
  postName: string
  postCode: string
  deptId: string | null
  organizationWide: boolean | null
  status: OrganizationStatus | null
}

export interface PostFormProps {
  initialModel?: PostItem
}

export interface PostMember {
  userId: string | number
  username: string
  name: string
  deptId: string | number
  status: OrganizationStatus
}

export interface PostMemberQuery {
  username: string
  name: string
  deptId: string | null
  status: OrganizationStatus | null
}

export interface PostMembersProps {
  post: PostItem
  departmentOptions: TreeSelectOption[]
  departmentNames: ReadonlyMap<string, string>
  canLoadDepartments: boolean
}
