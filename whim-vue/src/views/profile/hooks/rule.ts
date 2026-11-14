import type { FormRules } from 'naive-ui'
import { createPasswordRules, createUserRules } from '@/views/system/user/hooks/rule'

const userRules = createUserRules(
  () => true,
  () => '',
)

/** 本人资料与用户管理使用同一套姓名、联系方式及性别约束。 */
export const profileRules: FormRules = {
  name: userRules.name!,
  phone: userRules.phone!,
  email: userRules.email!,
  gender: userRules.gender!,
}

/** 本人改密复用新密码和确认密码规则，原密码按原输入校验。 */
export function createProfilePasswordRules(getPassword: () => string): FormRules {
  return {
    ...createPasswordRules(getPassword),
    oldPassword: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
      {
        trigger: ['blur', 'input'],
        validator: (_, value: string) =>
          !value ||
          (value.length <= 72 && new TextEncoder().encode(value).length <= 72) ||
          new Error('原密码不能超过72个字符，且UTF-8长度不超过72字节'),
      },
    ],
  }
}
