import axiosInstance, { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { ConfigFormModel, ConfigId, ConfigItem, ConfigQuery, SiteSettings } from './types'

export const fetchSiteSettings = () =>
  request<SiteSettings>('/system/config/site', RequestMethod.GET)

export const fetchConfigPage = (params: ConfigQuery & TablePageQuery & { builtIn: boolean }) =>
  request<TablePage<ConfigItem>>('/system/config/page', RequestMethod.GET, { params })

export const addConfig = (data: Omit<ConfigFormModel, 'imageFile'>) =>
  request('/system/config/add', RequestMethod.POST, { data })

export const updateConfig = (data: { id: ConfigId; configValue: string } & Partial<Pick<ConfigFormModel, 'configName' | 'valueType' | 'valueOptions' | 'remark'>>) =>
  request('/system/config/update', RequestMethod.PUT, { data })

export const deleteConfig = (id: ConfigId) =>
  request('/system/config/delete', RequestMethod.DELETE, { params: { id } })

export async function uploadConfigImage(file: File): Promise<string> {
  const data = new FormData()
  data.append('file', file)
  const response = await request<string>('/system/config/uploadImage', RequestMethod.POST, { data, timeout: 60000 })
  if (response.code !== 200 || !response.data) throw new Error(response.message || '配置图片上传失败')
  return String(response.data)
}

export async function fetchConfigImage(id: ConfigId, signal: AbortSignal): Promise<Blob> {
  const response = await axiosInstance.get<Blob>('/system/config/image', { params: { id }, responseType: 'blob', signal })
  return response.data
}
