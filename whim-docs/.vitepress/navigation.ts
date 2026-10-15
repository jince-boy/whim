export interface DocLink {
  text: string
  link: string
  description?: string
}

export interface DocGroup {
  text: string
  items: DocLink[]
}

export const navigation: DocGroup[] = [
  {
    text: '开始使用',
    items: [
      { text: '项目介绍', link: '/guide/introduction', description: '模块边界与能力地图' },
      { text: '启动文档站', link: '/guide/getting-started', description: '安装、构建与部署' },
      { text: '开发约定', link: '/guide/conventions', description: '页面、组件与服务的组织方式' },
    ],
  },
  {
    text: '自定义组件',
    items: [
      { text: '组件总览', link: '/components/overview' },
      { text: '数据表格 WhimTable', link: '/components/table' },
      { text: '查询表单 SearchForm', link: '/components/search-form' },
      { text: '表单与确认弹窗', link: '/components/dialog' },
      { text: '图标 Icon', link: '/components/icon' },
      { text: '图标选择 WhimSelectIcon', link: '/components/select-icon' },
      { text: '菜单搜索', link: '/components/menu-search' },
      { text: '主题设置', link: '/components/theme' },
    ],
  },
  {
    text: '后端指南',
    items: [
      { text: '架构与接入', link: '/backend/architecture' },
      { text: '数据权限', link: '/backend/data-permission' },
      { text: '自定义注解', link: '/backend/annotations' },
      { text: 'Sa-Token 权限注解', link: '/backend/satoken' },
      { text: '多账号认证', link: '/backend/multi-account' },
      { text: '会话与授权变更', link: '/backend/session' },
      { text: '核心工具与 Web 约定', link: '/backend/core' },
    ],
  },
]

export const pageLinks = navigation.flatMap((group) => group.items)
