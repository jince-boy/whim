import { request, RequestMethod } from '@/api'
import type { CaptchaResult, LoginRequest, LoginResult } from './types'

/**
 * 登录
 * @param data 登录参数
 */
export const login = (data: LoginRequest) => {
  return request<LoginResult>('/system/auth/login', RequestMethod.POST, { data })
}
/**
 * 退出登录
 */
export const logout = () => {
  return request<void>('/system/auth/logout', RequestMethod.POST)
}
/**
 * 获取验证码
 */
export const fetchCaptcha = (signal?: AbortSignal) => {
  return request<CaptchaResult>('/system/auth/captcha', RequestMethod.GET, { signal })
}
