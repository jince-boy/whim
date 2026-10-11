export interface LoginForm {
  username: string
  password: string
  uuid: string
  rememberMe: boolean
  captchaCode: string
}

export interface LoginResult {
  tokenType: string
  accessToken: string
  expiresIn: number
}

export interface CaptchaResult {
  uuid: string
  image: string
  expiresIn: number
}
