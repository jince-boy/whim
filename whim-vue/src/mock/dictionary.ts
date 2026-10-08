import type { DictTypeResult } from '@/views/system/dictType/hooks/types'
import type { DictDataResult } from '@/views/system/dictData/hooks/types'
import { database, persistDatabase } from './database'
import {
  currentTime,
  fail,
  filterRows,
  findById,
  paginate,
  readIds,
  success,
  type MockParams,
} from './utils'

/** 模拟字典类型的分页、详情、增删改及缓存重置。 */
export function handleDictType(path: string, method: string, params: MockParams, data: MockParams) {
  if (path === '/system/dictType/page' && method === 'GET') {
    return success(paginate(filterRows(database.dictTypes, params), params))
  }
  if (path === '/system/dictType/detail' && method === 'GET') {
    return success(findById(database.dictTypes, params.id))
  }
  if (path === '/system/dictType/reset' && method === 'DELETE') return success(null)
  if (['/system/dictType/add', '/system/dictType/update'].includes(path)) {
    const inserting = path.endsWith('/add') && method === 'POST'
    if (!inserting && !(path.endsWith('/update') && method === 'PUT')) return
    const existing = inserting ? undefined : findById(database.dictTypes, data.id)
    const dictType = { ...existing, ...data } as DictTypeResult
    if (!dictType.name?.trim() || !dictType.type?.trim()) fail('请填写字典名称和字典类型')
    if (
      database.dictTypes.some((item) => item.type === dictType.type && item.id !== existing?.id)
    ) {
      fail('字典类型已存在')
    }
    if (inserting) {
      database.dictTypes.push({
        ...dictType,
        id: crypto.randomUUID(),
        status: 0,
        createTime: currentTime(),
      })
    } else {
      const previousType = existing!.type
      Object.assign(existing!, dictType)
      for (const item of database.dictData) {
        if (item.dictType === previousType) item.dictType = dictType.type
      }
    }
    persistDatabase()
    return success(null)
  }
  if (path === '/system/dictType/delete' && method === 'DELETE') {
    const ids = readIds(params.dictTypeIds)
    const removedTypes = database.dictTypes
      .filter((item) => ids.includes(item.id))
      .map((item) => item.type)
    database.dictTypes = database.dictTypes.filter((item) => !ids.includes(item.id))
    database.dictData = database.dictData.filter((item) => !removedTypes.includes(item.dictType))
    persistDatabase()
    return success(null)
  }
}

/** 模拟字典选项、分页、详情和数据维护，保证同一类型内键值唯一。 */
export function handleDictData(path: string, method: string, params: MockParams, data: MockParams) {
  if (path.startsWith('/system/dictData/type/') && method === 'GET') {
    const dictType = decodeURIComponent(path.slice('/system/dictData/type/'.length))
    return success(
      database.dictData
        .filter((item) => item.dictType === dictType)
        .sort((a, b) => a.sort - b.sort),
    )
  }
  if (path === '/system/dictData/page' && method === 'GET') {
    const rows = filterRows(database.dictData, params).sort((a, b) => a.sort - b.sort)
    return success(paginate(rows, params))
  }
  if (path === '/system/dictData/detail' && method === 'GET') {
    return success(findById(database.dictData, params.id))
  }
  if (['/system/dictData/add', '/system/dictData/update'].includes(path)) {
    const inserting = path.endsWith('/add') && method === 'POST'
    if (!inserting && !(path.endsWith('/update') && method === 'PUT')) return
    const existing = inserting ? undefined : findById(database.dictData, data.id)
    const item = { ...existing, ...data } as DictDataResult
    if (!item.label?.trim() || !String(item.value ?? '').trim()) fail('请填写字典标签和字典键值')
    if (!database.dictTypes.some((type) => type.type === item.dictType)) fail('字典类型不存在')
    if (
      database.dictData.some(
        (row) =>
          row.dictType === item.dictType && row.value === item.value && row.id !== existing?.id,
      )
    ) {
      fail('当前字典类型下的键值已存在')
    }
    if (inserting) {
      database.dictData.push({ ...item, id: crypto.randomUUID(), createTime: currentTime() })
    } else {
      Object.assign(existing!, item)
    }
    persistDatabase()
    return success(null)
  }
  if (path === '/system/dictData/delete' && method === 'DELETE') {
    const ids = readIds(params.dictDataIds)
    database.dictData = database.dictData.filter((item) => !ids.includes(item.id))
    persistDatabase()
    return success(null)
  }
}
