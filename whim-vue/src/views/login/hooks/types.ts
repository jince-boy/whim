export interface LoginForm {
  username: string
  password: string
  uuid: string
  rememberMe: boolean
  captcha: number | null
}

export interface LoginResult {
  prefix: string
  token: string
  expire: number
}

export interface CaptchaResult {
  uuid: string
  image: string
}
