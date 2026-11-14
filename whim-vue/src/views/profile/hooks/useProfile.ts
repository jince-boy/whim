import { computed, ref } from 'vue'
import { isAxiosError } from 'axios'
import { useRouter } from 'vue-router'
import { useFormDialog } from '@/components/dialog'
import { message } from '@/plugins'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'
import { updateUserPassword, updateUserProfile, uploadProfileAvatar } from './api'
import type { ProfileFormModel } from './types'
import PasswordForm from '../PasswordForm.vue'

/** 保存本人个人资料，并同步当前账号的全局姓名、头像及资料。 */
export function useProfile() {
  const userStore = useUserStore()
  const auth = useAuthStore()
  const saving = ref(false)
  const router = useRouter()
  const { openFormDialog } = useFormDialog()
  const passwordDialogOpen = ref(false)

  /** 保存失败保留编辑草稿，旧账号请求不得覆盖后来登录的账号。 */
  async function saveProfile(model: ProfileFormModel) {
    if (saving.value) return
    const token = auth.getToken
    saving.value = true
    try {
      let avatar = model.avatar
      if (model.avatarFile) {
        const uploaded = await uploadProfileAvatar(model.avatarFile)
        // 上传与保存之间可能切换账号，旧草稿不能继续提交给新登录的用户。
        if (auth.getToken !== token) return
        if (uploaded.code !== 200) throw new Error(uploaded.message || '头像上传失败')
        avatar = uploaded.data
      }
      const response = await updateUserProfile({
        name: model.name.trim(),
        avatar,
        phone: model.phone?.trim() || null,
        email: model.email?.trim() || null,
        gender: model.gender,
      })
      if (auth.getToken !== token) return
      if (response.code !== 200) throw new Error(response.message || '个人资料保存失败')
      userStore.updateCurrentUserInfo(response.data)
      message.success(response.message || '个人资料保存成功')
    } catch (error) {
      if (auth.getToken === token && !isAxiosError(error)) {
        message.error(error instanceof Error ? error.message : '个人资料保存失败，请重试')
      }
    } finally {
      saving.value = false
    }
  }

  /** 本人改密不要求用户管理权限，成功后清理旧会话并返回登录页。 */
  function openPasswordDialog() {
    const user = userStore.user
    if (!user || saving.value || passwordDialogOpen.value) return
    const token = auth.getToken
    passwordDialogOpen.value = true
    openFormDialog({
      title: '修改密码',
      width: 500,
      formComponent: PasswordForm,
      formProps: { user },
      modalProps: {
        positiveText: '保存密码',
        positiveButtonProps: { type: 'warning' },
        onAfterLeave: () => {
          passwordDialogOpen.value = false
        },
      },
      onSubmit: async (model) => {
        // 弹窗属于打开时的账号，退出或切换登录后不能继续提交旧密码草稿。
        if (auth.getToken !== token) return false
        const result = await updateUserPassword({
          oldPassword: model.oldPassword,
          newPassword: model.password,
        })
        if (auth.getToken !== token) return false
        return result
      },
      onSuccess: async () => {
        if (auth.getToken !== token) return
        auth.logout()
        await router.replace('/login')
      },
    })
  }

  return { user: computed(() => userStore.user), saving, saveProfile, openPasswordDialog }
}
