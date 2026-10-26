import { execFile } from 'node:child_process'
import { mkdtemp, readFile, readdir, realpath, rm, writeFile } from 'node:fs/promises'
import { createRequire } from 'node:module'
import { tmpdir } from 'node:os'
import { basename, dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { promisify } from 'node:util'

const require = createRequire(import.meta.url)
const packageDirectory = dirname(require.resolve('@lucide/vue/package.json'))
const { version } = JSON.parse(await readFile(join(packageDirectory, 'package.json'), 'utf8'))
const targetDirectory = fileURLToPath(new URL('../src/components/icon/', import.meta.url))
const temporaryRoot = await realpath(tmpdir())
const temporaryDirectory = await mkdtemp(join(temporaryRoot, 'whim-lucide-metadata-'))
const archiveRoot = `lucide-${version}`
const archiveFile = join(temporaryDirectory, 'source.tar.gz')

try {
  console.info(`正在下载 Lucide ${version} 官方搜索元数据…`)
  const response = await fetch(
    `https://codeload.github.com/lucide-icons/lucide/tar.gz/refs/tags/${version}`,
    {
      signal: AbortSignal.timeout(30000),
    },
  )
  if (!response.ok) throw new Error(`官方数据下载失败：HTTP ${response.status}`)
  await writeFile(archiveFile, Buffer.from(await response.arrayBuffer()))

  // 只解压图标元数据所在目录和许可证，不执行上游仓库中的代码。
  await promisify(execFile)(
    'tar',
    [
      '-xzf',
      archiveFile,
      '-C',
      temporaryDirectory,
      `${archiveRoot}/icons`,
      `${archiveRoot}/LICENSE`,
    ],
    { timeout: 30000, windowsHide: true },
  )

  const iconNames = (await readdir(join(packageDirectory, 'dist/esm/icons')))
    .filter((name) => name.endsWith('.mjs') && name !== 'index.mjs')
    .map((name) => name.slice(0, -4))
    .sort()
  const icons = {}
  for (const name of iconNames) {
    const metadata = JSON.parse(
      await readFile(join(temporaryDirectory, archiveRoot, 'icons', `${name}.json`), 'utf8'),
    )
    icons[name] = {
      aliases: (metadata.aliases ?? []).map((alias) =>
        typeof alias === 'string' ? alias : alias.name,
      ),
      tags: metadata.tags ?? [],
      categories: metadata.categories ?? [],
    }
  }

  const license = await readFile(join(temporaryDirectory, archiveRoot, 'LICENSE'), 'utf8')
  const metadata = {
    version,
    source: `https://github.com/lucide-icons/lucide/tree/${version}/icons`,
    icons,
  }
  // 全部读取成功后再写入，离线构建直接使用已保存的数据。
  await writeFile(
    join(targetDirectory, 'iconSearchMetadata.json'),
    `${JSON.stringify(metadata, null, 2)}\n`,
  )
  await writeFile(join(targetDirectory, 'lucide.LICENSE'), license)
  console.info(`已同步 ${iconNames.length} 个图标的标签、别名和分类，版本：${version}`)
} finally {
  const cleanupDirectory = resolve(temporaryDirectory)
  if (
    dirname(cleanupDirectory) !== temporaryRoot ||
    !basename(cleanupDirectory).startsWith('whim-lucide-metadata-')
  ) {
    throw new Error('临时目录路径校验失败，已停止清理')
  }
  await rm(cleanupDirectory, { recursive: true, force: true, maxRetries: 2, retryDelay: 100 })
}
