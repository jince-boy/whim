import { computed, onScopeDispose, ref, useTemplateRef, watch } from 'vue'
import type { FormInst, SelectOption } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { useUserStore } from '@/stores/modules/user'
import { useAvatarImages } from '@/hooks/useAvatarImages'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { fetchPostPage } from '@/views/system/post/hooks/api'
import type { PostItem } from '@/views/system/post/hooks/types'
import { createUserRules } from './rule'
import { message } from '@/plugins'
import type { UserFormModel, UserFormProps } from './types'

/** 用户资料草稿、部门选择和分页岗位候选项。 */
export function useUserForm(props: UserFormProps) {
  const dictionary = useDictStore()
  const currentUser = useUserStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<UserFormModel>({
    username: '', name: '', avatar: null, avatarFile: null, password: '', confirmPassword: '',
    deptId: null, email: null, phone: null, gender: 0, status: 0, postIds: [], remark: null,
  })
  const isEdit = computed(() => props.initialModel != null)
  const avatarInput = useTemplateRef<HTMLInputElement>('avatarInput')
  const avatarPreview = ref<string | null>(null)
  const avatarName = computed(() => formModel.value.name || formModel.value.username || '用户')
  const avatarText = computed(() => avatarName.value.slice(0, 1).toUpperCase())
  const avatarSource = computed(() => avatarPreview.value || formModel.value.avatar)
  const { getAvatarUrl, avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading, openAvatarPreview } =
    useAvatarImages(() => [avatarSource.value])
  const avatarUrl = computed(() => getAvatarUrl(avatarSource.value))
  const avatarCropFile = ref<File | null>(null)
  const avatarCropVisible = ref(false)

  /** 选择本地图片，实际上传与用户资料提交共用弹窗的提交锁。 */
  function selectAvatar() {
    avatarInput.value?.click()
  }

  /** 保留原头像直到所选文件通过大小和类型校验，允许重选同名文件。 */
  function changeAvatar(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) return
    if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
      message.error('头像仅支持JPG、PNG或GIF图片')
      return
    }
    if (!file.size || file.size > 5 * 1024 * 1024) {
      message.error('头像图片不能为空且不能超过5MB')
      return
    }
    avatarCropFile.value = file
    avatarCropVisible.value = true
  }

  /** 裁剪确认后更新头像草稿，取消裁剪不会改变原头像。 */
  function confirmAvatarCrop(file: File) {
    formModel.value.avatarFile = file
  }

  /** 清空草稿中的头像，保存成功后才影响用户资料。 */
  function removeAvatar() {
    formModel.value.avatarFile = null
    formModel.value.avatar = null
  }

  watch(() => formModel.value.avatarFile, (file, _previous, onCleanup) => {
    const objectUrl = file ? URL.createObjectURL(file) : null
    avatarPreview.value = objectUrl
    onCleanup(() => { if (objectUrl) URL.revokeObjectURL(objectUrl) })
  })
  const isSelf = computed(() => isEdit.value && String(props.initialModel?.id) === String(currentUser.user?.userId))
  const isBuiltInAdministrator = computed(() => props.initialModel?.username === 'admin')
  const organizationReadonly = computed(() => isSelf.value && !isBuiltInAdministrator.value)
  const selfEditNotice = computed(() => isBuiltInAdministrator.value
    ? 'admin账号仅限本人维护，可修改个人资料、部门和岗位，不能停用或删除当前登录账号。'
    : '当前账号的部门、岗位和状态需由其他管理员调整。')
  const { selection, loading, error, canLoad, reload } = useDeptOptions(() => formModel.value.status === 0)
  const departmentOptions = computed(() => {
    const current = props.initialModel
    if (current?.deptId != null && !selection.value.names.has(String(current.deptId))) {
      return [{ key: String(current.deptId), label: current.deptName || '当前部门' }, ...selection.value.options]
    }
    return selection.value.options
  })
  const canLoadPosts = computed(() =>
    currentUser.permissionCode.includes('*') || currentUser.permissionCode.includes('system:post:list'))
  const postLoading = ref(false)
  const postError = ref('')
  const posts = ref<PostItem[]>([])
  const knownPosts = ref(new Map<string, PostItem>())
  const postPattern = ref('')
  const postPage = ref(0)
  const postTotal = ref(0)
  let postVersion = 0

  /** 岗位搜索使用真实分页，切换关键词时丢弃过期响应，保留已选项的回显。 */
  async function loadPosts(reset = false) {
    if (!canLoadPosts.value || organizationReadonly.value || (!reset && postLoading.value)) return
    const version = reset ? ++postVersion : postVersion
    const pageNum = reset ? 1 : postPage.value + 1
    if (reset) posts.value = []
    postLoading.value = true
    postError.value = ''
    try {
      const response = await fetchPostPage({
        pageNum, pageSize: 20, postName: postPattern.value.trim(), postCode: '',
        deptId: null, organizationWide: null, status: 0,
      })
      if (version !== postVersion) return
      if (response.code !== 200) throw new Error(response.message || '岗位加载失败')
      posts.value = reset ? response.data.data : [...posts.value, ...response.data.data]
      for (const post of response.data.data) knownPosts.value.set(String(post.id), post)
      postPage.value = pageNum
      postTotal.value = response.data.total
    } catch {
      if (version === postVersion) postError.value = '岗位加载失败，请重新搜索或重试'
    } finally {
      if (version === postVersion) postLoading.value = false
    }
  }

  function searchPosts(pattern: string) {
    postPattern.value = pattern
    void loadPosts(true)
  }

  function loadMorePosts(event: Event) {
    const element = event.target as HTMLElement
    if (element.scrollTop + element.clientHeight >= element.scrollHeight - 24 &&
      posts.value.length < postTotal.value && !postError.value) void loadPosts()
  }

  const postOptions = computed<SelectOption[]>(() => {
    const options = new Map(posts.value.map((post) => [String(post.id), post]))
    for (const id of formModel.value.postIds) {
      const post = knownPosts.value.get(id)
      if (post) options.set(id, post)
    }
    return [...options.values()].map((post) => ({
      value: String(post.id),
      label: `${post.postName}（${post.postCode}）${post.status === 0 ? '' : ' · ' + dictionary.getDictData('sys_common_status', post.status).label}`,
    }))
  })

  watch(() => [props.initialModel, props.defaultDeptId] as const, ([model, defaultDeptId]) => {
    formModel.value = {
      id: model?.id, username: model?.username ?? '', name: model?.name ?? '',
      avatar: model?.avatar ?? null, avatarFile: null, password: '', confirmPassword: '',
      deptId: model ? model.deptId == null ? null : String(model.deptId) : (defaultDeptId ?? null),
      email: model?.email ?? null, phone: model?.phone ?? null,
      gender: model?.gender ?? 0, status: model?.status ?? 0,
      postIds: model?.posts.map((post) => String(post.id)) ?? [], remark: model?.remark ?? null,
    }
    knownPosts.value = new Map((model?.posts ?? []).map((post) => [String(post.id), post]))
    formRef.value?.restoreValidation()
    searchPosts('')
  }, { immediate: true })
  onScopeDispose(() => postVersion++)

  return {
    formRef, formModel, isEdit, isSelf, organizationReadonly, selfEditNotice,
    avatarName, avatarText, avatarUrl, selectAvatar, changeAvatar, removeAvatar,
    avatarSource, avatarPreviewUrl, avatarPreviewShown, avatarPreviewLoading, openAvatarPreview,
    avatarCropFile, avatarCropVisible, confirmAvatarCrop,
    rules: computed(() => createUserRules(() => isEdit.value, () => formModel.value.password)),
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    genderOptions: computed(() => dictionary.getDictNumberOptions('sys_user_gender')),
    departmentOptions, departmentLoading: loading, departmentError: error,
    canLoadDepartments: canLoad, reloadDepartments: reload,
    canLoadPosts, postOptions, postLoading, postError, searchPosts, loadMorePosts,
    retryPosts: () => loadPosts(true),
  }
}

