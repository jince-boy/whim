import { defineAsyncComponent, type Component } from 'vue'
import { commonIcons, iconAliases } from './commonIcons'

export type IconName = string
export interface IconOption {
  name: IconName
  label: string
  keywords?: string
  aliases?: readonly string[]
  tags?: readonly string[]
  categories?: readonly string[]
}

const iconComponents = new Map<string, Component>()

/** 打开选择器时加载完整目录；浏览器会缓存模块，后续打开无需重复下载。 */
export async function loadIconCatalog(): Promise<readonly IconOption[]> {
  return (await import('./lucideCatalog')).iconCatalog
}

/** 将菜单图标名称规范为当前 Lucide 的正式名称。 */
export function getIconName(name?: string | null): string {
  const iconName = name?.trim() ?? ''
  return Object.hasOwn(iconAliases, iconName) ? iconAliases[iconName]! : iconName
}

/** 优先使用常用组件，其余图标按需加载并缓存。 */
export function getIconComponent(name?: string | null): Component | undefined {
  const iconName = getIconName(name)
  if (!iconName) return undefined
  if (Object.hasOwn(commonIcons, iconName)) return commonIcons[iconName]
  const cached = iconComponents.get(iconName)
  if (cached) return cached

  const component = defineAsyncComponent({
    loader: async () => (await import('./lucideCatalog')).loadIconComponent(iconName),
    delay: 0,
    timeout: 10000,
    loadingComponent: commonIcons['circle-dashed'],
    errorComponent: commonIcons['circle-question-mark'],
    /** 短暂网络失败时重试一次，避免无限请求。 */
    onError(_error, retry, fail, attempts) {
      if (attempts === 1) retry()
      else fail()
    },
  })
  iconComponents.set(iconName, component)
  return component
}
