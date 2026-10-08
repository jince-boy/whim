import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { saveAs } from 'file-saver'
import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth.ts'

/** 接口统一响应结构。 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

// 未显式配置时，开发环境使用本地数据，生产构建使用真实接口。
export const isMockEnabled =
  import.meta.env.VITE_USE_MOCK === 'true' ||
  (import.meta.env.VITE_USE_MOCK !== 'false' && import.meta.env.DEV)

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  // 在传输层替换请求，普通请求、下载和直接使用实例的调用均不会访问后端。
  adapter: isMockEnabled
    ? async (config) => {
        const { mockAdapter } = await import('@/mock')
        return mockAdapter(config)
      }
    : undefined,
})

let redirectingToLogin = false

/** 读取普通响应或 JSON Blob 中的错误信息，保留原始响应数据。 */
const readResponseBody = async (
  response: AxiosResponse<unknown>,
): Promise<Partial<ApiResponse> | undefined> => {
  let data = response.data
  if (data instanceof Blob) {
    const contentType = String(response.headers['content-type'] || data.type)
    if (!/(\/|\+)json\b/i.test(contentType)) return
    try {
      data = JSON.parse(await data.text())
    } catch {
      return
    }
  }
  if (typeof data === 'object' && data !== null) {
    return data as Partial<ApiResponse>
  }
}

/** 清理失效登录状态并跳转，合并并发请求触发的登录跳转。 */
const handleUnauthorized = async (message: string) => {
  if (redirectingToLogin) return
  redirectingToLogin = true
  try {
    const currentRoute = router.currentRoute.value
    const redirect = window.location.pathname + window.location.search + window.location.hash
    useAuthStore().logout()
    window.$message?.warning(message)
    if (currentRoute.path !== '/login') {
      await router.replace({
        path: '/login',
        query: { redirect: encodeURIComponent(redirect) },
      })
    }
  } finally {
    redirectingToLogin = false
  }
}

// 请求统一携带当前登录令牌。
axiosInstance.interceptors.request.use(
  (config) => {
    const token = useAuthStore().getToken
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 拦截器保留完整响应，普通请求由 request 提取 data，下载读取响应头。
axiosInstance.interceptors.response.use(
  async (response: AxiosResponse<unknown>) => {
    const body = await readResponseBody(response)
    if (body?.code === 401) {
      const message = body.message || '登录状态已失效，请重新登录'
      await handleUnauthorized(message)
      throw new AxiosError(message, 'ERR_UNAUTHORIZED', response.config, response.request, response)
    }
    if (response.config.responseType === 'blob' && body?.code !== undefined && body.code !== 200) {
      const message = body.message || '文件下载失败，请稍后重试'
      window.$message?.error(message)
      throw new AxiosError(message, 'ERR_BAD_RESPONSE', response.config, response.request, response)
    }
    return response
  },
  async (error: unknown) => {
    if (!axios.isAxiosError(error) || axios.isCancel(error)) throw error
    const body = error.response ? await readResponseBody(error.response) : undefined
    const unauthorized = error.response?.status === 401 || body?.code === 401
    let message = '网络连接失败，请检查网络'
    if (unauthorized) {
      message = '登录状态已失效，请重新登录'
    } else if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      message = '请求超时，请稍后重试'
    } else if (error.response) {
      message = '请求失败，请稍后重试'
    }
    error.message = body?.message || message
    if (unauthorized) {
      await handleUnauthorized(error.message)
    } else {
      window.$message?.error(error.message)
    }
    throw error
  },
)

export enum RequestMethod {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
  PATCH = 'PATCH',
}

/** 发送 JSON 接口请求并返回统一响应，其他业务状态码交由调用方处理。 */
export const request = async <T = unknown>(
  url: string,
  method: RequestMethod = RequestMethod.GET,
  config?: Omit<AxiosRequestConfig, 'url' | 'method' | 'responseType'>,
): Promise<ApiResponse<T>> => {
  const response = await axiosInstance.request<ApiResponse<T>>({
    ...config,
    url,
    method,
    responseType: 'json',
  })
  return response.data
}
/**
 * 下载文件
 * @param url 文件请求地址
 * @param fileName 保存的文件名
 * @param config 可选的 Axios 配置
 */
export const downloadFile = async (
  url: string,
  fileName?: string | null,
  config?: Omit<AxiosRequestConfig, 'url' | 'responseType'>,
): Promise<void> => {
  try {
    const response = await axiosInstance.request<Blob>({
      ...config,
      url,
      method: config?.method || RequestMethod.GET,
      responseType: 'blob',
    })
    if (!fileName) {
      const disposition = String(response.headers['content-disposition'] || '')
      if (disposition) {
        // 优先使用 filename* 的 UTF-8 编码，支持语言标记和引号。
        const extended = disposition.match(/(?:^|;)\s*filename\*\s*=\s*(?:"([^"]*)"|([^;]*))/i)
        const encoded = (extended?.[1] || extended?.[2])?.trim().match(/^UTF-8'[^']*'(.*)$/i)
        if (encoded) {
          try {
            fileName = decodeURIComponent(encoded[1])
          } catch {
            // 无效百分号编码时回退到普通 filename。
          }
        }
        if (!fileName) {
          const plain = disposition.match(
            /(?:^|;)\s*filename\s*=\s*(?:"((?:\\.|[^"\\])*)"|([^;]*))/i,
          )
          fileName = plain?.[1]?.replace(/\\(.)/g, '$1') || plain?.[2]?.trim()
        }
      }
    }
    // 文件名只保留末段，缺失时使用默认名称。
    fileName = fileName?.split(/[/\\]/).pop() || `download-${Date.now()}`
    saveAs(response.data, fileName)
  } catch (error) {
    console.error('文件下载失败', error)
    throw error
  }
}
export default axiosInstance
