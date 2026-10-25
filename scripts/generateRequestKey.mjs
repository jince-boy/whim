#!/usr/bin/env node
import { generateKeyPairSync } from 'node:crypto'

// 生成一对 RSA 密钥，输出可直接复制到前后端配置的单行 Base64 字符串。
try {
  const { privateKey, publicKey } = generateKeyPairSync('rsa', {
    modulusLength: 3072,
    publicExponent: 65537,
    privateKeyEncoding: { type: 'pkcs8', format: 'der' },
    publicKeyEncoding: { type: 'spki', format: 'der' },
  })

  process.stdout.write(
    '# 后端：whim-start/src/main/resources/application.yml 的 whim.request-encryption 下\n' +
      `private-key: "${privateKey.toString('base64')}"\n\n` +
      '# 前端：whim-vue/.env\n' +
      `VITE_REQUEST_ENCRYPTION_PUBLIC_KEY="${publicKey.toString('base64')}"\n`,
  )
} catch {
  process.stderr.write('生成请求加密密钥失败，请检查当前 Node.js 环境是否支持 RSA 密钥生成。\n')
  process.exitCode = 1
}
