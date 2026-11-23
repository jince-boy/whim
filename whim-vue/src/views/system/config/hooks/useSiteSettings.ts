import systemSetting, { deploymentSettings } from '@/config/SystemSetting'
import { fetchSiteSettings } from './api'
import type { ConfigItem, SiteSettings } from './types'

const siteSettingFields = new Map<string, keyof SiteSettings>([
  ['system.site.logo', 'logoFileId'],
  ['system.site.title', 'title'],
  ['system.site.description', 'description'],
  ['system.site.copyright', 'copyright'],
])

let revision = 0

export function getSiteSettingField(item?: ConfigItem) {
  return item?.builtIn ? siteSettingFields.get(item.configKey) : undefined
}

/** 列表与编辑框使用同一生效值；网站配置留空时沿用部署设置。 */
export function getEffectiveConfigValue(item: ConfigItem): string {
  const field = getSiteSettingField(item)
  return field ? item.configValue?.trim() || deploymentSettings[field] : item.configValue ?? ''
}

/** 页面初始化和配置修改后读取同一份公开信息，较早的响应不能覆盖新设置。 */
export async function refreshSiteSettings() {
  const current = ++revision
  const response = await fetchSiteSettings()
  if (current !== revision || response.code !== 200) return
  for (const field of siteSettingFields.values()) {
    systemSetting[field] = response.data[field]?.trim() || deploymentSettings[field]
  }
}
