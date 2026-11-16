import axios, {
  AxiosError,
  CanceledError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from 'axios'
import { calculateJwkThumbprint, CompactEncrypt, exportJWK } from 'jose'
import type { ApiResponse } from './index'

const bodyMethods = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])
let encryptionKey: Promise<{ publicKey: CryptoKey; keyId: string }> | undefined
let publicKeyRequest: Promise<string> | undefined

/** 从真实后端获取公钥，并发请求共用初始化结果，失败后允许重试。 */
const getPublicKeyBase64 = (client: AxiosInstance): Promise<string> => {
  if (!publicKeyRequest) {
    publicKeyRequest = client
      .get<ApiResponse<string>>('/system/auth/requestEncryptionKey')
      .then(({ data }) => {
        if (
          data.code !== 200 ||
          typeof data.data !== 'string' ||
          !/^[A-Za-z0-9+/]+={0,2}$/.test(data.data)
        ) {
          throw new Error('后端返回的请求加密公钥无效')
        }
        return data.data
      })
      .catch(() => {
        publicKeyRequest = undefined
        throw new Error('获取请求加密公钥失败，请检查后端服务后重试')
      })
  }
  return publicKeyRequest
}

/** 使用浏览器原生能力导入后端公钥，同一页面只解析一次。 */
const getEncryptionKey = (
  publicKeyBase64: string,
): Promise<{ publicKey: CryptoKey; keyId: string }> => {
  if (!encryptionKey) {
    encryptionKey = (async () => {
      const publicKey = await crypto.subtle.importKey(
        'spki',
        Uint8Array.from(atob(publicKeyBase64), (character) => character.charCodeAt(0)),
        { name: 'RSA-OAEP', hash: 'SHA-256' },
        true,
        ['encrypt'],
      )
      if ((publicKey.algorithm as RsaHashedKeyAlgorithm).modulusLength < 3072) {
        throw new Error('请求加密 RSA 公钥长度不能小于 3072 位')
      }
      const keyId = await calculateJwkThumbprint(await exportJWK(publicKey))
      return { publicKey, keyId }
    })()
  }
  return encryptionKey
}

/** 统一加密 JSON 对象和数组，首次发送前自动获取公钥。 */
export const encryptRequest = async (
  config: InternalAxiosRequestConfig,
  client: AxiosInstance,
): Promise<void> => {
  const method = (config.method || 'GET').toUpperCase()
  if (!bodyMethods.has(method) || config.data == null) return
  const isJsonBody =
    Array.isArray(config.data) ||
    (typeof config.data === 'object' && Object.getPrototypeOf(config.data) === Object.prototype)
  // 文件、表单等非 JSON 请求保持原有传输方式；强制加密接口仍会在后端拒绝它们。
  if (!isJsonBody) return
  try {
    if (
      !config.url?.startsWith('/') ||
      config.url.startsWith('//') ||
      config.baseURL !== import.meta.env.VITE_API_BASE_URL
    ) {
      throw new Error('业务请求必须使用项目统一的接口地址')
    }
    const target = new URL(config.url, window.location.origin)
    if (target.search || target.hash || (config.params && Object.keys(config.params).length)) {
      throw new Error('加密请求的业务参数必须放在 JSON 请求体中')
    }
    if (config.signal?.aborted) throw new CanceledError()
    // 公钥请求复用公共 HTTP 客户端；GET 没有请求体，不会再次进入加密初始化。
    const publicKeyBase64 = await getPublicKeyBase64(client)
    const payload = JSON.stringify({ method, path: target.pathname, body: config.data })
    let encryptedData: string
    if (globalThis.crypto?.subtle) {
      const { publicKey, keyId } = await getEncryptionKey(publicKeyBase64)
      encryptedData = await new CompactEncrypt(new TextEncoder().encode(payload))
        .setProtectedHeader({
          alg: 'RSA-OAEP-256',
          enc: 'A256GCM',
          kid: keyId,
          typ: 'whim-request+jwe',
          cty: 'application/json',
        })
        .encrypt(publicKey)
    } else {
      // HTTP 不提供 SubtleCrypto，按需加载兼容实现并保持相同的 JWE 协议。
      const { encryptRequestWithoutWebCrypto } = await import('./requestEncryptionFallback')
      encryptedData = encryptRequestWithoutWebCrypto(payload, publicKeyBase64)
    }
    if (config.signal?.aborted) throw new CanceledError()
    config.data = { encryptedData }
    config.headers.setContentType('application/json')
  } catch (error) {
    if (axios.isCancel(error)) throw error
    // 原始请求可能含密码，不把 config 或第三方异常附加到错误对象。
    const message = error instanceof Error ? error.message : '请求加密失败，请刷新页面后重试'
    throw new AxiosError(message, 'ERR_REQUEST_ENCRYPTION')
  }
}
