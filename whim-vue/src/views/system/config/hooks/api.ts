import { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/table'
import type { ConfigFormModel, ConfigId, ConfigItem, ConfigQuery, SiteSettings } from './types'

export const fetchSiteSettings = () =>
  request<SiteSettings>('/system/config/site', RequestMethod.GET)

export const fetchConfigPage = (params: ConfigQuery & TablePageQuery & { builtIn: boolean }) =>
  request<TablePage<ConfigItem>>('/system/config/page', RequestMethod.GET, { params })

export const addConfig = (data: ConfigFormModel) =>
  request('/system/config/add', RequestMethod.POST, { data })

export const updateConfig = (data: { id: ConfigId; configValue: string } & Partial<Pick<ConfigFormModel, 'configName' | 'valueType' | 'valueOptions' | 'remark'>>) =>
  request('/system/config/update', RequestMethod.PUT, { data })

export const deleteConfig = (id: ConfigId) =>
  request('/system/config/delete', RequestMethod.DELETE, { params: { id } })
