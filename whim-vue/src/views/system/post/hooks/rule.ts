import type { FormRules } from 'naive-ui'

export const postRules: FormRules = {
  postName: [
    { required: true, whitespace: true, message: '请输入岗位名称', trigger: 'blur' },
    { max: 64, message: '岗位名称不能超过64个字符', trigger: 'blur' },
  ],
  postCode: [
    { required: true, whitespace: true, message: '请输入岗位编码', trigger: 'blur' },
    { max: 64, message: '岗位编码不能超过64个字符', trigger: 'blur' },
  ],
  status: {
    type: 'enum',
    enum: [0, 1],
    required: true,
    message: '请选择状态',
    trigger: 'change',
  },
  sort: {
    type: 'integer',
    required: true,
    min: 0,
    max: 2147483647,
    message: '请输入有效的非负整数排序值',
    trigger: ['blur', 'change'],
  },
  remark: { max: 255, message: '备注不能超过255个字符', trigger: 'blur' },
}
