import type { UserInfoResult } from '@/stores/modules/user/types'

export interface ProfileUpdate {
  name: string
  avatar: string | null
  phone: string | null
  email: string | null
  gender: number
}

export interface ProfileFormModel extends ProfileUpdate {
  avatarFile: File | null
}

export interface ProfileFormProps {
  initialModel: UserInfoResult
  saving: boolean
}

export interface ProfilePasswordUpdate {
  oldPassword: string
  newPassword: string
}

export interface ProfilePasswordFormModel {
  oldPassword: string
  password: string
  confirmPassword: string
}

export interface ProfilePasswordFormProps {
  user: UserInfoResult
}

export interface TwoFactorSetup {
  setupId: string
  secret: string
  recoveryCode: string
}

export interface TwoFactorConfirm {
  setupId: string
  code: string
}

export interface TwoFactorDisable {
  password: string
  code: string
}

/** 只在本次验证器弹窗内使用，关闭时整份丢弃。 */
export interface TwoFactorSettingsDraft {
  disabling: boolean
  password: string
  code: string
  savedRecovery: boolean
  submitting: boolean
  setup?: TwoFactorSetup
}
