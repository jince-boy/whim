export const technologyGroups = [
  {
    title: '前端技术栈',
    label: 'Frontend',
    items: [
      ['Vue 3.5', '界面框架'],
      ['TypeScript 6', '类型系统'],
      ['Naive UI 2', '表单与表格'],
      ['Pinia 4', '状态管理'],
      ['Vue Router 5', '页面路由'],
      ['Vite 8', '开发与构建'],
      ['Axios', 'HTTP 请求'],
      ['ECharts 6', '图表可视化'],
    ],
  },
  {
    title: '后端技术栈',
    label: 'Backend',
    items: [
      ['Java 25', '运行与编译'],
      ['Spring Boot 4.1.1', '应用框架'],
      ['MyBatis-Plus 3.5.15', '持久化'],
      ['Sa-Token 1.46.0', '身份与权限'],
      ['Redisson 4.3.0', 'Redis 客户端'],
      ['Lock4j 2.2.7', '分布式锁'],
      ['Apache Fesod', 'Excel 读写'],
      ['Maven', '多模块构建'],
    ],
  },
  {
    title: '基础设施',
    label: 'Infra',
    items: [
      ['MySQL', '业务数据库'],
      ['Redis', '缓存与会话'],
      ['Flyway', '数据库迁移'],
      ['Docker Compose', '前后端容器部署'],
    ],
  },
] as const

export const capabilities = [
  {
    icon: 'shield-check',
    title: '认证与授权',
    description: '通过角色控制菜单、操作和数据范围，支持手机验证器动态码和恢复码。',
  },
  {
    icon: 'network',
    title: '用户与组织',
    description: '按部门管理用户和负责人，支持多岗位任职与多角色授权。',
  },
  {
    icon: 'folder-open',
    title: '文件与表格',
    description: '文件支持本地、MinIO 和阿里云 OSS 存储，Excel 模块提供流式读写。',
  },
  {
    icon: 'bell',
    title: '公告与推送',
    description: '公告发布后通过 SSE 提醒在线用户，也可向指定用户或全部在线用户推送事件。',
  },
  {
    icon: 'activity',
    title: '审计与监控',
    description: '查看登录与操作日志，监控服务器、缓存、数据库、接口和在线用户。',
  },
  {
    icon: 'settings',
    title: '系统配置',
    description: '维护字典、站点标题、Logo 和功能开关，可按用户或角色配置首页。',
  },
] as const
