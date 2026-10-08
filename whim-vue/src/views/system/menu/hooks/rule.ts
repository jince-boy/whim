import type { FormRules } from 'naive-ui'

export const menuRules: FormRules = {
  parentId: [
    { type: 'string', required: true, message: '请输入上级菜单', trigger: ['blur', 'change'] },
  ],
  type: [{ type: 'number', required: true, trigger: 'change', message: '请选择菜单类型' }],
  name: [{ type: 'string', required: true, message: '请输入路由名称', trigger: 'blur' }],
  title: [{ type: 'string', required: true, message: '请输入菜单名称', trigger: 'blur' }],
  code: [{ type: 'string', required: true, message: '请输入权限标识', trigger: 'blur' }],
  redirect: [{ type: 'string', required: true, message: '请输入外链地址', trigger: 'blur' }],
  path: [{ type: 'string', required: true, message: '请输入路由地址', trigger: 'blur' }],
  component: [{ type: 'string', required: true, message: '请输入组件路径', trigger: 'blur' }],
  sort: [{ type: 'number', required: true, message: '请输入排序', trigger: 'blur' }],
  icon: [{ type: 'string', required: true, message: '请选择图标', trigger: 'blur' }],
  visible: [{ type: 'number', required: true, message: '请选择显示状态', trigger: 'change' }],
  status: [{ type: 'number', required: true, message: '请选择菜单状态', trigger: 'change' }],
  keepAlive: [{ type: 'number', required: true, message: '请选择缓存状态', trigger: 'change' }],
}
