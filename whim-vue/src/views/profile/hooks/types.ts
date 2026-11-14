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
