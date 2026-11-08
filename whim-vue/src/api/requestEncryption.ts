import axios, { AxiosError, CanceledError, type InternalAxiosRequestConfig } from 'axios'
import { calculateJwkThumbprint, CompactEncrypt, exportJWK } from 'jose'

const bodyMethods = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])
let encryptionKey: Promise<{ publicKey: CryptoKey; keyId: string }> | undefined

/** 读取并校验两种加密实现共用的固定公钥配置。 */
const getPublicKeyBase64 = (): string => {
  const publicKeyBase64 = import.meta.env.VITE_REQUEST_ENCRYPTION_PUBLIC_KEY?.trim()
  if (!publicKeyBase64) throw new Error('请配置请求加密公钥 VITE_REQUEST_ENCRYPTION_PUBLIC_KEY')
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(publicKeyBase64)) {
    throw new Error('请求加密公钥必须是无头尾标记和换行的单行 Base64 字符串')
  }
  return publicKeyBase64
}

/** 使用浏览器原生能力导入固定公钥，同一页面只解析一次。 */
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

/** 统一加密 JSON 对象和数组，业务接口无需加密标识，也不发送额外请求。 */
export const encryptRequest = async (config: InternalAxiosRequestConfig): Promise<void> => {
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
    const publicKeyBase64 = getPublicKeyBase64()
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
    const message = error instanceof Error ? error.message : '请求加密失败，请检查公钥配置'
    throw new AxiosError(message, 'ERR_REQUEST_ENCRYPTION')
  }
}
