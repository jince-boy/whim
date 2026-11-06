import { computed, onScopeDispose, ref, useTemplateRef, watch } from 'vue'
import type { FormInst, SelectOption } from 'naive-ui'
import { useDictStore } from '@/stores/modules/dict'
import { useUserStore } from '@/stores/modules/user'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { fetchPostPage } from '@/views/system/post/hooks/api'
import type { PostItem } from '@/views/system/post/hooks/types'
import { createUserRules } from './rule'
import type { UserFormModel, UserFormProps } from './types'

/** 用户资料草稿、部门选择和分页岗位候选项。 */
export function useUserForm(props: UserFormProps) {
  const dictionary = useDictStore()
  const currentUser = useUserStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<UserFormModel>({
    username: '', name: '', password: '', confirmPassword: '',
    deptId: null, email: null, phone: null, gender: 0, status: 0, postIds: [], remark: null,
  })
  const isEdit = computed(() => props.initialModel != null)
  const isSelf = computed(() => isEdit.value && String(props.initialModel?.id) === String(currentUser.user?.userId))
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
    if (!canLoadPosts.value || isSelf.value || (!reset && postLoading.value)) return
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
      password: '', confirmPassword: '',
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
    formRef, formModel, isEdit, isSelf,
    rules: computed(() => createUserRules(() => isEdit.value, () => formModel.value.password)),
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    genderOptions: computed(() => dictionary.getDictNumberOptions('sys_user_gender')),
    departmentOptions, departmentLoading: loading, departmentError: error,
    canLoadDepartments: canLoad, reloadDepartments: reload,
    canLoadPosts, postOptions, postLoading, postError, searchPosts, loadMorePosts,
    retryPosts: () => loadPosts(true),
  }
}

