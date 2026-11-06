import type { FormRules } from 'naive-ui'

/** 创建用户与修改密码共用密码约束，字节长度与后端 BCrypt 限制一致。 */
export function createPasswordRules(getPassword: () => string): FormRules {
  return {
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      {
        trigger: ['blur', 'input'],
        validator: (_, value: string) =>
          !value || (value.length >= 8 && value.length <= 72 && !/\s/u.test(value) &&
            new TextEncoder().encode(value).length <= 72) ||
          new Error('密码需为8～72个字符，不含空白，且UTF-8长度不超过72字节'),
      },
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      {
        trigger: ['blur', 'input'],
        validator: (_, value: string) => value === getPassword() || new Error('两次输入的密码不一致'),
      },
    ],
  }
}

/** 编辑资料不校验密码，新增账号必须设置初始密码。 */
export function createUserRules(isEdit: () => boolean, getPassword: () => string): FormRules {
  return {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { pattern: /^[A-Za-z0-9_]{3,32}$/, message: '用户名为3～32位字母、数字或下划线', trigger: 'blur' },
    ],
    name: [
      { required: true, whitespace: true, message: '请输入姓名', trigger: 'blur' },
      { max: 24, message: '姓名不能超过24个字符', trigger: 'blur' },
    ],
    email: [
      { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
      { max: 64, message: '邮箱不能超过64个字符', trigger: 'blur' },
    ],
    phone: { max: 20, message: '联系电话不能超过20个字符', trigger: 'blur' },
    gender: { required: true, type: 'enum', enum: [0, 1, 2], message: '请选择性别', trigger: 'change' },
    status: { required: true, type: 'enum', enum: [0, 1], message: '请选择状态', trigger: 'change' },
    postIds: { type: 'array', max: 100, message: '最多选择100个岗位', trigger: 'change' },
    remark: { max: 255, message: '备注不能超过255个字符', trigger: 'blur' },
    ...(isEdit() ? {} : createPasswordRules(getPassword)),
  }
}

export const roleRules: FormRules = {
  roleIds: { type: 'array', max: 100, message: '最多选择100个角色', trigger: 'change' },
}

