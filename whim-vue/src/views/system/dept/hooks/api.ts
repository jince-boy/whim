import { request, RequestMethod } from '@/api'
import type {
  DeptId,
  DeptItem,
  DeptLeaderOption,
  DeptLeaderQuery,
  DeptQuery,
  DeptSave,
  OrganizationStatusUpdate,
} from './types'
import type { TablePage } from '@/components/table'

/** 查询当前用户可见的部门树。 */
export const fetchDeptTree = (params?: Partial<DeptQuery>) =>
  request<DeptItem[]>('/system/dept/list', RequestMethod.GET, { params })

export const fetchDeptDetail = (id: DeptId) =>
  request<DeptItem>('/system/dept/detail', RequestMethod.GET, { params: { id } })

/** 负责人仅从已归属当前部门的正常用户中选择。 */
export const fetchDeptLeaderOptions = (params: DeptLeaderQuery) =>
  request<TablePage<DeptLeaderOption>>('/system/dept/leaderOptions', RequestMethod.GET, {
    params,
  })

/** 新增部门，仅返回操作结果。 */
export const addDept = (data: DeptSave) =>
  request('/system/dept/add', RequestMethod.POST, { data: getDeptPayload(data) })

export const updateDept = (data: DeptSave) =>
  request('/system/dept/update', RequestMethod.PUT, { data: getDeptPayload(data) })

export const updateDeptStatus = (data: OrganizationStatusUpdate) =>
  request('/system/dept/status', RequestMethod.PUT, { data })

export const deleteDept = (id: DeptId) =>
  request('/system/dept/delete', RequestMethod.DELETE, { params: { id } })

/** 清空字段显式提交 null，用户 ID 始终按文本传输。 */
function getDeptPayload(data: DeptSave): DeptSave {
  return {
    ...data,
    deptName: data.deptName.trim(),
    deptCode: data.deptCode.trim(),
    leaderUserId: data.leaderUserId?.trim() || null,
    phone: data.phone?.trim() || null,
    email: data.email?.trim() || null,
    remark: data.remark?.trim() || null,
  }
}
