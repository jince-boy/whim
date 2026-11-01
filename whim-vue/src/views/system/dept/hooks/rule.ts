import type { FormRules } from 'naive-ui'
import type { Ref } from 'vue'

export function createDeptRules(
  parentValid: Readonly<Ref<boolean>>,
  leaderValid: Readonly<Ref<boolean>>,
): FormRules {
  return {
    parentId: {
      required: true,
      validator: () => parentValid.value,
      message: '请选择可用的上级部门，或等待部门加载完成',
      trigger: 'change',
    },
    deptName: [
      { required: true, whitespace: true, message: '请输入部门名称', trigger: 'blur' },
      { max: 64, message: '部门名称不能超过64个字符', trigger: 'blur' },
    ],
    deptCode: [
      { required: true, whitespace: true, message: '请输入部门编码', trigger: 'blur' },
      { max: 64, message: '部门编码不能超过64个字符', trigger: 'blur' },
    ],
    leaderUserId: {
      validator: () => leaderValid.value,
      message: '请从本部门正常用户中选择负责人，或清空已失效的负责人',
      trigger: 'change',
    },
    phone: { max: 20, message: '联系电话不能超过20个字符', trigger: 'blur' },
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
      { max: 64, message: '邮箱不能超过64个字符', trigger: 'blur' },
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
}
