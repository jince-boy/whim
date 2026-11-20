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
