import { request, RequestMethod } from '@/api'
import type { TablePage, TablePageQuery } from '@/components/dataTable'
import type { HomeId, HomePageQuery, HomePageRow, HomePageSave, HomeTarget } from './types'

const base = '/system/homePage'
export const fetchHomePageConfigs = (params: HomePageQuery & TablePageQuery) =>
  request<TablePage<HomePageRow>>(`${base}/page`, RequestMethod.GET, { params })

export const fetchHomeTargets = (
  params: TablePageQuery & { targetType: number; keyword: string },
) => request<TablePage<HomeTarget>>(`${base}/targets`, RequestMethod.GET, { params })

export const addHomePage = (data: HomePageSave) =>
  request(`${base}/add`, RequestMethod.POST, { data })

export const updateHomePage = (data: HomePageSave) =>
  request(`${base}/update`, RequestMethod.PUT, { data })

export const deleteHomePage = (id: HomeId) =>
  request(`${base}/delete`, RequestMethod.DELETE, { params: { id } })
