import { AxiosError } from 'axios'
import type { ApiResponse } from '@/api'

export type MockParams = Record<string, unknown>

/** 构造与真实接口相同的成功响应。 */
export function success<T>(data: T, message = '操作成功'): ApiResponse<T> {
  return { code: 200, message, data }
}

/** 抛出明确的模拟业务错误，由适配器补齐 HTTP 响应交给统一拦截器。 */
export function fail(message: string, status = 400): never {
  throw new AxiosError(message, String(status))
}

/** 读取 Axios 已序列化的 JSON 请求体或查询对象。 */
export function readParams(value: unknown): MockParams {
  if (typeof value === 'string') return JSON.parse(value) as MockParams
  if (value instanceof URLSearchParams) return Object.fromEntries(value)
  return value && typeof value === 'object' ? (value as MockParams) : {}
}

/** 读取数字或字符串 ID 数组，兼容批量删除参数。 */
export function readIds(value: unknown): string[] {
  if (value === undefined || value === null || value === '') fail('请选择要删除的数据')
  return (Array.isArray(value) ? value : String(value).split(',')).map(String)
}

/** 查询详情并在记录不存在时返回明确错误。 */
export function findById<T extends { id: string }>(rows: T[], id: unknown): T {
  const row = rows.find((item) => item.id === String(id))
  if (!row) fail('数据不存在，请刷新后重试', 404)
  return row
}

/** 按当前页面的字段筛选，状态、类型精确匹配，其余文本支持模糊匹配。 */
export function filterRows<T extends object>(
  rows: T[],
  params: MockParams,
  timeField?: keyof T,
): T[] {
  return rows.filter((row) => {
    const values = row as MockParams
    return Object.entries(params).every(([key, value]) => {
      if (value === '' || value === null || value === undefined) return true
      if (['pageNum', 'pageSize'].includes(key)) return true
      if (key === 'startTime' || key === 'endTime') {
        if (!timeField) return true
        const time = String(row[timeField])
        return key === 'startTime' ? time >= String(value) : time <= String(value)
      }
      const actual = key === 'ipAddress' && 'operIp' in values ? values.operIp : values[key]
      if (['status', 'logType', 'dictType'].includes(key)) return String(actual) === String(value)
      return String(actual ?? '')
        .toLowerCase()
        .includes(String(value).trim().toLowerCase())
    })
  })
}

/** 根据筛选结果计算分页，删除末页数据后自动回到有效页码。 */
export function paginate<T>(rows: T[], params: MockParams) {
  const requestedSize = Number(params.pageSize) || 10
  const size = Math.max(1, Math.min(100, Math.trunc(requestedSize)))
  const pages = Math.ceil(rows.length / size)
  const currentPage = Math.max(1, Math.min(pages || 1, Math.trunc(Number(params.pageNum) || 1)))
  return {
    currentPage,
    data: rows.slice((currentPage - 1) * size, currentPage * size),
    pages,
    size,
    total: rows.length,
  }
}

/** 返回用于新增记录的本地时间，与现有页面的时间格式保持一致。 */
export function currentTime(): string {
  const now = new Date()
  const parts = [
    now.getMonth() + 1,
    now.getDate(),
    now.getHours(),
    now.getMinutes(),
    now.getSeconds(),
  ].map((value) => String(value).padStart(2, '0'))
  return `${now.getFullYear()}-${parts[0]}-${parts[1]} ${parts[2]}:${parts[3]}:${parts[4]}`
}

/** 导出带 UTF-8 BOM 的 CSV，转义单元格并避免文本被表格软件解释为公式。 */
export function exportCsv<T extends object>(rows: T[]): Blob {
  const fields = Object.keys(rows[0] ?? {})
  /** 转义单元格中的引号、换行和公式前缀。 */
  const escapeCell = (value: unknown): string => {
    let text = String(value ?? '')
    if (/^[=+@\-\t\r\n]/.test(text)) text = `'${text}`
    return `"${text.replaceAll('"', '""')}"`
  }
  const lines = [fields.map(escapeCell).join(',')]
  for (const row of rows) {
    lines.push(fields.map((field) => escapeCell((row as MockParams)[field])).join(','))
  }
  return new Blob(['\uFEFF', lines.join('\r\n')], { type: 'text/csv;charset=utf-8' })
}
