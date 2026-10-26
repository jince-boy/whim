import type { LucideIcon } from '@lucide/vue'
import type { IconOption } from './catalog'
import { commonIcons } from './commonIcons'
import { iconLabels } from './iconLabels'
import iconSearchMetadata from './iconSearchMetadata.json'

const searchMetadata: Readonly<
  Record<string, Pick<IconOption, 'aliases' | 'tags' | 'categories'>>
> = iconSearchMetadata.icons

// 构建时生成懒加载函数，排除汇总模块及已静态引用的常用图标。
const modules = import.meta.glob<LucideIcon>(
  [
    '/node_modules/@lucide/vue/dist/esm/icons/*.mjs',
    '!**/index.mjs',
    '!**/{house,layout-dashboard,menu,settings,user,user-round,users,shield,shield-check,network,briefcase-business,book-open,database,clipboard-list,chart-column,logs,scroll-text,file-text,file-spreadsheet,folder,folder-open,search,plus,pencil,trash,download,upload,refresh-cw,save,copy,check,x,minus,ellipsis,eye,eye-off,sliders-horizontal,list,grip,palette,sun,moon,monitor,monitor-cog,maximize,minimize,app-window,panel-top,panels-top-left,bell,circle-question-mark,info,triangle-alert,circle-check,circle-x,log-in,log-out,lock-keyhole,key-round,power,keyboard,corner-down-left,arrow-left,arrow-right,arrow-up,arrow-down,arrow-left-right,chevron-left,chevron-right,chevron-up,chevron-down,chevrons-left,chevrons-right,chevrons-up,chevrons-down,link,globe,languages,map-pin,map-pinned,camera,image,wallet,face-slightly-smiling,thumbs-up,thumbs-down,mars,venus,calendar-days,clock,mail,activity,code,circle-dashed}.mjs',
  ],
  { import: 'default', exhaustive: true },
)
const iconLoaders = new Map(
  Object.entries(modules).map(([path, loader]) => [
    path.slice(path.lastIndexOf('/') + 1, -4),
    loader,
  ]),
)
const labels = Object.entries(iconLabels)
export const iconCatalog: readonly IconOption[] = [
  ...new Set([...Object.keys(commonIcons), ...iconLoaders.keys()]),
]
  .sort()
  .map((name) => ({
    ...searchMetadata[name],
    name,
    label: iconLabels[name] ?? name,
    keywords: [
      ...labels.filter(([prefix]) => name.startsWith(`${prefix}-`)).map(([, label]) => label),
    ].join(' '),
  }))

/** 按名称加载单个组件，目录中不存在的名称使用问号占位。 */
export function loadIconComponent(name: string): Promise<LucideIcon> {
  if (Object.hasOwn(commonIcons, name)) return Promise.resolve(commonIcons[name]!)
  return iconLoaders.get(name)?.() ?? Promise.resolve(commonIcons['circle-question-mark']!)
}
