import { request, RequestMethod } from '@/api'
import type { CaptchaResult, LoginResult } from './types'

/**
 * 登录
 * @param data 登录参数
 */
export const login = (data: object) => {
  return request<LoginResult>('/system/auth/login', RequestMethod.POST, { data })
}
/**
 * 退出登录
 */
export const logout = () => {
  return request('/system/auth/logout', RequestMethod.POST)
}
/**
 * 获取验证码
 */
export const fetchCaptcha = () => {
  return request<CaptchaResult>('/system/auth/captcha', RequestMethod.GET)
}
