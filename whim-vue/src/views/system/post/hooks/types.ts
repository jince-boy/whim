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
  /** 所选部门及全部下级部门；为空时不按部门筛选。 */
  deptId: string | null
  organizationWide: boolean | null
  status: OrganizationStatus | null
}

export interface PostFormProps {
  initialModel?: PostItem
  defaultDeptId?: string | null
}
