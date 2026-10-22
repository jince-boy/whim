import { sha256 } from '@noble/hashes/sha2.js'
import { md5 } from '@noble/hashes/legacy.js'
import { bytesToHex } from '@noble/hashes/utils.js'
import type { HashRequest, HashReply } from './types'

/** 在 Worker 内增量计算文件指纹或存储兼容校验值，内存不随文件总大小增长。 */
self.addEventListener('message', async (event: MessageEvent<HashRequest>) => {
  try {
    const { file, mode } = event.data
    const hash = mode === 'fingerprint' ? sha256.create() : md5.create()
    const strongHash = mode === 'part' ? sha256.create() : undefined
    const chunkSize = 4 * 1024 * 1024
    for (let offset = 0; offset < file.size; offset += chunkSize) {
      const bytes = new Uint8Array(await file.slice(offset, offset + chunkSize).arrayBuffer())
      hash.update(bytes)
      strongHash?.update(bytes)
      self.postMessage({
        progress: Math.min(1, (offset + chunkSize) / file.size),
      } satisfies HashReply)
    }
    const digest = hash.digest()
    if (strongHash) {
      self.postMessage({
        checksums: {
          contentMd5: btoa(String.fromCharCode(...digest)),
          contentSha256: btoa(String.fromCharCode(...strongHash.digest())),
        },
      } satisfies HashReply)
    } else {
      self.postMessage({ value: bytesToHex(digest) } satisfies HashReply)
    }
  } catch {
    self.postMessage({ error: '文件读取或校验失败，请重新选择文件' } satisfies HashReply)
  }
})
