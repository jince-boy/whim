import type { InputHTMLAttributes } from 'vue'

export interface PasswordInputProps {
  /** 用户名、姓名等相关词，用于识别包含个人信息的易猜密码。 */
  userInputs?: string[]
  inputProps?: InputHTMLAttributes
}

