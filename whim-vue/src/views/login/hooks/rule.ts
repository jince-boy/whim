import type { FormRules } from 'naive-ui'

export const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度在3到32个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度在8到72个字符', trigger: 'blur' },
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]{4}$/, message: '请输入4位字母或数字验证码', trigger: 'blur' },
  ],
}
