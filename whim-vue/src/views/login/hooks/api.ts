import { request, RequestMethod } from '@/api'
import type { CaptchaResult, LoginRequest, LoginResult, LoginChallenge, LoginRecovery, VerifyLoginRequest } from './types'

/**
 * 登录
 * @param data 登录参数
 * @param signal 登录页离开时取消请求
 */
export const login = (data: LoginRequest, signal: AbortSignal) => {
  return request<LoginResult | LoginChallenge>('/system/auth/login', RequestMethod.POST, { data, signal })
}
/** 凭短时挑战完成第二步登录，或用恢复码解除旧绑定。 */
export const verifyLogin = (data: VerifyLoginRequest, signal: AbortSignal) => {
  return request<LoginResult | LoginRecovery>('/system/auth/verifyLogin', RequestMethod.POST, { data, signal })
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
