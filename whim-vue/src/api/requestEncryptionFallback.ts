import forge from 'node-forge'

let encryptionKey: { publicKey: forge.pki.rsa.PublicKey; keyId: string } | undefined

/** 将二进制字符串编码为 JWE 使用的无填充 Base64URL。 */
const encodeBase64Url = (bytes: string): string =>
  forge.util.encode64(bytes).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')

/** 将 RSA 正整数编码为 JWK 要求的最短无符号字节串。 */
const encodePublicInteger = (integer: forge.jsbn.BigInteger): string => {
  const hexadecimal = integer.toString(16)
  return encodeBase64Url(
    forge.util.hexToBytes(hexadecimal.length % 2 ? `0${hexadecimal}` : hexadecimal),
  )
}

/** 从浏览器密码学随机源获取字节，不使用兼容库的随机数回退。 */
const getRandomBytes = (length: number): string => {
  if (!globalThis.crypto?.getRandomValues) {
    throw new Error('当前浏览器不支持密码学随机数，请更新浏览器后重试')
  }
  return String.fromCharCode(...crypto.getRandomValues(new Uint8Array(length)))
}

/** 在没有 SubtleCrypto 的 HTTP 页面生成与后端兼容的 JWE 密文。 */
export const encryptRequestWithoutWebCrypto = (
  payload: string,
  publicKeyBase64: string,
): string => {
  if (!encryptionKey) {
    const publicKey = forge.pki.publicKeyFromAsn1(
      forge.asn1.fromDer(forge.util.decode64(publicKeyBase64)),
    )
    if (publicKey.n.bitLength() < 3072) {
      throw new Error('请求加密 RSA 公钥长度不能小于 3072 位')
    }
    // RFC 7638 按字段名排序并省略可选字段，确保与原生实现及后端指纹一致。
    const thumbprint = JSON.stringify({
      e: encodePublicInteger(publicKey.e),
      kty: 'RSA',
      n: encodePublicInteger(publicKey.n),
    })
    const keyId = encodeBase64Url(
      forge.md.sha256.create().update(thumbprint, 'utf8').digest().getBytes(),
    )
    encryptionKey = { publicKey, keyId }
  }

  const protectedHeader = encodeBase64Url(
    JSON.stringify({
      alg: 'RSA-OAEP-256',
      enc: 'A256GCM',
      kid: encryptionKey.keyId,
      typ: 'whim-request+jwe',
      cty: 'application/json',
    }),
  )
  const contentEncryptionKey = getRandomBytes(32)
  const initializationVector = getRandomBytes(12)
  const encryptedKey = encryptionKey.publicKey.encrypt(contentEncryptionKey, 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: { md: forge.md.sha256.create() },
    seed: getRandomBytes(32),
  })
  const cipher = forge.cipher.createCipher('AES-GCM', contentEncryptionKey)
  // JWE 将受保护头的 Base64URL 文本作为附加认证数据，防止头部被篡改。
  cipher.start({ iv: initializationVector, additionalData: protectedHeader, tagLength: 128 })
  cipher.update(forge.util.createBuffer(payload, 'utf8'))
  if (!cipher.finish()) throw new Error('请求加密失败')

  return [
    protectedHeader,
    encodeBase64Url(encryptedKey),
    encodeBase64Url(initializationVector),
    encodeBase64Url(cipher.output.getBytes()),
    encodeBase64Url(cipher.mode.tag.getBytes()),
  ].join('.')
}
