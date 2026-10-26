import type { FormRules } from 'naive-ui'

export const dictTypeRules: FormRules = {
  name: [
    { required: true, whitespace: true, message: '请输入字典名称', trigger: 'blur' },
    { max: 64, message: '字典名称不能超过64个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    {
      pattern: /^[A-Za-z][A-Za-z0-9_]*$/,
      message: '编码须以字母开头，只能包含字母、数字和下划线',
      trigger: 'blur',
    },
    { max: 128, message: '类型编码不能超过128个字符', trigger: 'blur' },
  ],
  remark: [{ max: 255, message: '备注不能超过255个字符', trigger: 'blur' }],
}
