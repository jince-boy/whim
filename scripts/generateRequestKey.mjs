#!/usr/bin/env node
import { generateKeyPairSync } from 'node:crypto'
import { mkdirSync, writeFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'

// 可选的预生成工具，供多实例预先分发同一私钥；日常开发和部署无需执行。
try {
  const keyPath = resolve(process.argv[2] || './data/keys/request-encryption-private.key')
  const { privateKey } = generateKeyPairSync('rsa', {
    modulusLength: 3072,
    publicExponent: 65537,
    privateKeyEncoding: { type: 'pkcs8', format: 'der' },
  })
  mkdirSync(dirname(keyPath), { recursive: true, mode: 0o700 })
  writeFileSync(keyPath, privateKey.toString('base64'), { flag: 'wx', mode: 0o600 })
  process.stdout.write(`请求加密私钥已保存至 ${keyPath}，前端会自动获取对应公钥。\n`)
} catch (error) {
  process.stderr.write(
    error.code === 'EEXIST'
      ? '私钥文件已存在，未覆盖。请继续使用现有密钥。\n'
      : '生成请求加密私钥失败，请检查目录写入权限和 Node.js RSA 支持。\n',
  )
  process.exitCode = 1
}
