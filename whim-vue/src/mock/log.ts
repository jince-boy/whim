import { database, persistDatabase } from './database'
import { filterRows, paginate, readIds, success, type MockParams } from './utils'

/** 模拟两类日志的条件分页、批量删除和清空，沿用各自的 ID 参数。 */
export function handleLog(path: string, method: string, params: MockParams) {
  if (path === '/system/loginLog/page' && method === 'GET') {
    return success(paginate(filterRows(database.loginLogs, params, 'loginTime'), params))
  }
  if (path === '/system/operLog/page' && method === 'GET') {
    return success(paginate(filterRows(database.operLogs, params, 'operTime'), params))
  }
  if (path === '/system/loginLog/delete' && method === 'DELETE') {
    const ids = readIds(params.loginLogIds)
    database.loginLogs = database.loginLogs.filter((item) => !ids.includes(item.id))
  } else if (path === '/system/operLog/delete' && method === 'DELETE') {
    const ids = readIds(params.operLogIds)
    database.operLogs = database.operLogs.filter((item) => !ids.includes(item.id))
  } else if (path === '/system/loginLog/clean' && method === 'DELETE') {
    database.loginLogs = []
  } else if (path === '/system/operLog/clean' && method === 'DELETE') {
    database.operLogs = []
  } else {
    return
  }
  persistDatabase()
  return success(null)
}
