export interface LoginRequest {
  username: string
  password: string
  uuid?: string
  captchaCode?: string
}

export interface LoginForm extends LoginRequest {
  uuid: string
  captchaCode: string
  rememberMe: boolean
}

export interface LoginResult {
  tokenType: string
  accessToken: string
  expiresIn: number
}

export type CaptchaResult = {
  enabled: false
} | {
  enabled: true
  uuid: string
  image: string
  expiresIn: number
}

/** 密码通过后可能需要提交验证器动态码。 */
export interface LoginChallenge {
  challengeId: string
}

export interface VerifyLoginRequest {
  challengeId: string
  code: string
}

/** 使用恢复码会解除旧绑定，并要求重新密码登录。 */
export interface LoginRecovery {
  recovered: true
}
