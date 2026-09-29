import axios, { type AxiosRequestConfig } from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/**
 * @author Jince
 * @date 2026/09/29
 * @description 表示 HTTP 请求或业务响应中的错误。
 */
export class ApiError extends Error {
  readonly status: number | undefined

  /** 创建包含 HTTP 或业务状态码的错误。 */
  constructor(message: string, status?: number, options?: ErrorOptions) {
    super(message, options)
    this.name = 'ApiError'
    this.status = status
  }
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL?.trim() || '/api',
  timeout: 15_000,
})

/** 附加登录令牌，同时尊重调用方显式传入的认证头。 */
http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')

  if (token && !config.headers.has('Authorization')) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }

  return config
})

/** 将 HTTP、超时与网络错误转换为统一错误，保留取消请求的原始错误。 */
http.interceptors.response.use(undefined, (error: unknown) => {
  if (axios.isCancel(error) || !axios.isAxiosError<ApiResponse<unknown>>(error)) {
    return Promise.reject(error)
  }

  const status = error.response?.status
  const responseMessage = error.response?.data?.message
  let message: string

  if (typeof responseMessage === 'string' && responseMessage.trim()) {
    message = responseMessage
  } else if (status) {
    message = `请求失败（HTTP ${status}）`
  } else if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
    message = '请求超时，请稍后重试'
  } else {
    message = '网络连接失败，请检查网络后重试'
  }

  return Promise.reject(new ApiError(message, status, { cause: error }))
})

/** 调用返回 Result<T> 的接口，保留业务数据和成功消息。 */
export async function request<T>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  const response = await http.request<ApiResponse<T>>(config)
  const result = response.data

  if (!result || typeof result.code !== 'number' || typeof result.message !== 'string') {
    throw new ApiError('服务器响应格式错误', response.status)
  }

  if (result.code < 200 || result.code >= 300) {
    throw new ApiError(result.message || '请求失败', result.code)
  }

  return result
}
