import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { useAvatarImages } from '@/hooks/useAvatarImages'
import { message } from '@/plugins'
import type { ProfileFormModel, ProfileFormProps } from './types'

/** 维护本人资料草稿、头像裁剪与校验，确认保存前不改变登录用户信息。 */
export function useProfileForm(props: ProfileFormProps, save: (model: ProfileFormModel) => void) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('profileForm')
  const avatarInput = useTemplateRef<HTMLInputElement>('avatarInput')
  const validating = ref(false)
  const formModel = ref<ProfileFormModel>({
    name: '',
    avatar: null,
    phone: null,
    email: null,
    gender: 0,
    avatarFile: null,
  })
  const localAvatarUrl = ref<string>()
  const avatarCropFile = ref<File | null>(null)
  const avatarCropShown = ref(false)
  const avatarSource = computed(() => localAvatarUrl.value || formModel.value.avatar)
  const {
    getAvatarUrl,
    avatarPreviewUrl,
    avatarPreviewShown,
    avatarPreviewLoading,
    openAvatarPreview,
  } = useAvatarImages(() => [avatarSource.value])
  const avatarUrl = computed(() => getAvatarUrl(avatarSource.value))
  const displayName = computed(
    () => formModel.value.name.trim() || props.initialModel.name || props.initialModel.username,
  )
  const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
  const isDirty = computed(() => {
    const initial = props.initialModel
    const model = formModel.value
    return (
      !!model.avatarFile ||
      model.name !== (initial.name ?? '') ||
      model.avatar !== (initial.avatar ?? null) ||
      (model.phone || null) !== (initial.phone || null) ||
      (model.email || null) !== (initial.email || null) ||
      model.gender !== (initial.gender ?? 0)
    )
  })
  const disabled = computed(() => props.saving || validating.value)
  const roles = computed(() =>
    props.initialModel.roleInfoList?.length
      ? props.initialModel.roleInfoList.map((role) => ({
          code: role.roleCode,
          name: role.roleName,
        }))
      : props.initialModel.roleCodeSet.map((code) => ({ code, name: code })),
  )
  const department = computed(
    () => props.initialModel.deptName || (props.initialModel.deptId == null ? '未分配部门' : '—'),
  )
  const posts = computed(() => props.initialModel.postNames ?? [])

  /** 还原服务器中的资料，取消待保存头像并清理校验提示。 */
  function resetProfile() {
    const initial = props.initialModel
    formModel.value = {
      name: initial.name ?? '',
      avatar: initial.avatar ?? null,
      phone: initial.phone ?? null,
      email: initial.email ?? null,
      gender: initial.gender ?? 0,
      avatarFile: null,
    }
    avatarCropShown.value = false
    avatarCropFile.value = null
    formRef.value?.restoreValidation()
  }

  watch(() => props.initialModel, resetProfile, { immediate: true })
  watch(
    () => formModel.value.avatarFile,
    (file, _previous, onCleanup) => {
      const url = file ? URL.createObjectURL(file) : undefined
      localAvatarUrl.value = url
      onCleanup(() => {
        if (url) URL.revokeObjectURL(url)
      })
    },
  )

  /** 打开本地选图入口，提交过程中禁止更换草稿。 */
  function selectAvatar() {
    if (!disabled.value) avatarInput.value?.click()
  }

  /** 先校验本地图片再裁剪，取消选图或裁剪不会替换现有头像。 */
  function changeAvatar(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file || disabled.value) return
    if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
      message.error('头像仅支持JPG、PNG或GIF图片')
      return
    }
    if (!file.size || file.size > 5 * 1024 * 1024) {
      message.error('头像图片不能为空且不能超过5MB')
      return
    }
    avatarCropFile.value = file
    avatarCropShown.value = true
  }

  /** 将裁剪结果加入草稿，保存资料时才上传文件。 */
  function confirmAvatarCrop(file: File) {
    if (!disabled.value) formModel.value.avatarFile = file
  }

  /** 移除草稿头像，重置仍能恢复尚未保存的操作。 */
  function removeAvatar() {
    if (disabled.value) return
    formModel.value.avatarFile = null
    formModel.value.avatar = null
  }

  /** 草稿头像预览直接使用裁剪结果，已保存头像读取原文件。 */
  function previewAvatar() {
    void openAvatarPreview(avatarSource.value)
  }

  /** 校验后发送独立提交快照，接口失败时继续保留当前编辑内容。 */
  async function submitProfile() {
    if (disabled.value || !isDirty.value || avatarCropShown.value || !formRef.value) return
    validating.value = true
    try {
      try {
        await formRef.value.validate()
      } catch (error) {
        if (!Array.isArray(error)) message.error('资料校验失败，请重试')
        return
      }
      save({ ...formModel.value })
    } finally {
      validating.value = false
    }
  }

  return {
    formModel,
    disabled,
    isDirty,
    roles,
    department,
    posts,
    displayName,
    avatarText,
    avatarUrl,
    genderOptions: computed(() => dictionary.getDictNumberOptions('sys_user_gender')),
    avatarCropFile,
    avatarCropShown,
    avatarPreviewUrl,
    avatarPreviewShown,
    avatarPreviewLoading,
    selectAvatar,
    changeAvatar,
    confirmAvatarCrop,
    removeAvatar,
    previewAvatar,
    resetProfile,
    submitProfile,
  }
}
