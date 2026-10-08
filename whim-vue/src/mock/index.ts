import {
  AxiosError,
  AxiosHeaders,
  CanceledError,
  type AxiosAdapter,
  type AxiosResponse,
} from 'axios'
import { getMockUserInfo, handleAuth, requireMockLogin } from './auth'
import { handleMenu } from './menu'
import { handleDictData, handleDictType } from './dictionary'
import { handleLog } from './log'
import { database } from './database'
import { exportCsv, fail, filterRows, readParams, success, type MockParams } from './utils'
import type { ApiResponse } from '@/api'

/** 根据现有导出接口生成本地 CSV，保持下载方法及响应头读取方式不变。 */
function handleExport(path: string, method: string, params: MockParams) {
  if (method !== 'GET') return
  switch (path) {
    case '/system/dictType/export':
      return { name: '字典类型.csv', blob: exportCsv(filterRows(database.dictTypes, params)) }
    case '/system/dictData/export':
      return { name: '字典数据.csv', blob: exportCsv(filterRows(database.dictData, params)) }
    case '/system/loginLog/export':
      return {
        name: '登录日志.csv',
        blob: exportCsv(filterRows(database.loginLogs, params, 'loginTime')),
      }
    case '/system/operLog/export':
      return {
        name: '操作日志.csv',
        blob: exportCsv(filterRows(database.operLogs, params, 'operTime')),
      }
  }
}

/** 在浏览器内完成请求；未匹配的接口明确报错，绝不回退到后端。 */
export const mockAdapter: AxiosAdapter = async (config) => {
  if (config.signal?.aborted) throw new CanceledError('请求已取消', config)
  const response: AxiosResponse = {
    config,
    status: 200,
    statusText: 'OK',
    headers: new AxiosHeaders({ 'content-type': 'application/json;charset=utf-8' }),
    data: null,
  }
  try {
    const url = new URL(config.url ?? '', 'http://mock.local')
    const prefix = config.baseURL ?? ''
    const path =
      prefix && url.pathname.startsWith(`${prefix}/`)
        ? url.pathname.slice(prefix.length)
        : url.pathname
    const method = (config.method ?? 'GET').toUpperCase()
    const params = { ...Object.fromEntries(url.searchParams), ...readParams(config.params) }
    const data = readParams(config.data)
    let result: ApiResponse | undefined = handleAuth(path, method, data)
    if (!result) {
      requireMockLogin(config.headers.get('Authorization'))
      if (path === '/system/user/getInfo' && method === 'GET') {
        result = getMockUserInfo()
      } else if (path === '/system/auth/logout' && method === 'POST') {
        result = success(null, '退出成功')
      } else {
        const file = handleExport(path, method, params)
        if (file) {
          response.headers = new AxiosHeaders({
            'content-type': file.blob.type,
            'content-disposition': `attachment; filename*=UTF-8''${encodeURIComponent(file.name)}`,
          })
          response.data = file.blob
          return response
        }
        result =
          handleMenu(path, method, params, data) ??
          handleDictType(path, method, params, data) ??
          handleDictData(path, method, params, data) ??
          handleLog(path, method, params)
      }
    }
    if (!result) fail(`未配置本地测试接口：${method} ${path}`, 404)
    response.data =
      config.responseType === 'blob'
        ? new Blob([JSON.stringify(result)], { type: 'application/json' })
        : structuredClone(result)
    return response
  } catch (error) {
    const status = error instanceof AxiosError ? Number(error.code) || 500 : 500
    const message = error instanceof AxiosError ? error.message : '本地测试数据处理失败'
    const body = { code: status, message, data: null }
    response.status = status
    response.statusText = String(status)
    response.data =
      config.responseType === 'blob'
        ? new Blob([JSON.stringify(body)], { type: 'application/json' })
        : body
    throw new AxiosError(message, 'ERR_BAD_RESPONSE', config, undefined, response)
  }
}
