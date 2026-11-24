import type { FormRules } from 'naive-ui'

export const homePageRules: FormRules = {
  targetType: { type: 'number', required: true, message: '请选择关联类型', trigger: 'change' },
  targetId: {
    required: true,
    message: '请选择关联对象',
    trigger: 'change',
  },
  path: {
    required: true,
    pattern: /^(\/[A-Za-z][A-Za-z0-9]*)+$/,
    message: '请输入站内静态路由，例如 /finance/dashboard',
    trigger: 'blur',
  },
  component: { required: true, whitespace: true, message: '请输入页面组件路径', trigger: 'blur' },
  priority: {
    type: 'integer',
    required: true,
    min: 0,
    max: 2147483647,
    message: '请输入非负整数优先级',
    trigger: 'blur',
  },
  status: { type: 'number', required: true, message: '请选择状态', trigger: 'change' },
}
