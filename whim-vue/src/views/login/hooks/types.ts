export interface LoginRequest {
  username: string
  password: string
  uuid: string
  captchaCode: string
}

export interface LoginForm extends LoginRequest {
  rememberMe: boolean
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
