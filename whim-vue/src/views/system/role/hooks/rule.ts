import type { FormRules } from 'naive-ui'

export const roleRules: FormRules = {
  roleName: [
    { required: true, whitespace: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 32, message: '角色名称不能超过32个字符', trigger: 'blur' },
  ],
  roleCode: [
    { required: true, whitespace: true, message: '请输入角色编码', trigger: 'blur' },
    { max: 128, message: '角色编码不能超过128个字符', trigger: 'blur' },
  ],
  sort: {
    required: true,
    type: 'integer',
    min: 0,
    max: 2147483647,
    message: '请输入有效的非负整数排序值',
    trigger: ['blur', 'change'],
  },
  status: { required: true, type: 'enum', enum: [0, 1], message: '请选择状态', trigger: 'change' },
  remark: { max: 255, message: '备注不能超过255个字符', trigger: 'blur' },
}

export const permissionRules: FormRules = {
  permissionIds: {
    type: 'array',
    required: true,
    max: 5000,
    message: '最多选择5000项菜单权限',
    trigger: 'change',
  },
}

export function createDataScopeRules(
  isCustom: () => boolean,
  departmentsReady: () => boolean,
): FormRules {
  return {
    dataScope: {
      required: true,
      type: 'enum',
      enum: [1, 2, 3, 4, 5],
      message: '请选择数据范围',
      trigger: 'change',
    },
    deptIds: [
      { type: 'array', max: 1000, message: '最多选择1000个部门', trigger: 'change' },
      {
        trigger: 'change',
        validator: (_, value: string[]) =>
          !isCustom() ||
          (departmentsReady() && value.length > 0) ||
          new Error(
            departmentsReady() ? '请选择至少一个部门' : '请先取得部门查询权限并成功加载部门',
          ),
      },
    ],
  }
}
