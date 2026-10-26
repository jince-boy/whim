import type { FormRules } from 'naive-ui'

export const dictDataRules: FormRules = {
  type: [{ required: true, message: '请先选择所属字典类型', trigger: 'blur' }],
  label: [
    { required: true, whitespace: true, message: '请输入字典标签', trigger: 'blur' },
    { max: 128, message: '字典标签不能超过128个字符', trigger: 'blur' },
  ],
  value: [
    { required: true, whitespace: true, message: '请输入字典值', trigger: 'blur' },
    { max: 128, message: '字典值不能超过128个字符', trigger: 'blur' },
  ],
  listClass: [{ required: true, message: '请选择展示样式', trigger: 'change' }],
  sort: [
    {
      type: 'integer',
      required: true,
      min: 0,
      max: 2147483647,
      message: '排序须为0至2147483647之间的整数',
      trigger: ['blur', 'change'],
    },
  ],
  remark: [{ max: 255, message: '备注不能超过255个字符', trigger: 'blur' }],
}
