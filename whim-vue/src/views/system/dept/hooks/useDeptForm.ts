import { useDictStore } from '@/stores/modules/dict'
import { computed, onScopeDispose, ref, shallowRef, useTemplateRef, watch } from 'vue'
import type { FormInst, SelectOption, TreeSelectOption } from 'naive-ui'
import { useDeptOptions } from './useDeptOptions'
import { createDeptRules } from './rule'
import type { DeptFormProps, DeptItem, DeptSave } from './types'
import { fetchDeptLeaderOptions } from './api'

/** 编辑草稿与原数据隔离，父级选择排除自身和全部下级。 */
export function useDeptForm(props: DeptFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const { departments, loading, error, reload } = useDeptOptions()
  const leaderResults = shallowRef<SelectOption[]>([])
  const selectedLeader = shallowRef<SelectOption>()
  const leaderLoading = ref(false)
  const leaderError = ref('')
  const leaderKeyword = ref('')
  const leaderPage = ref(0)
  const leaderHasMore = ref(false)
  let leaderRevision = 0
  let searchTimer: ReturnType<typeof setTimeout> | undefined
  const formModel = ref<DeptSave>({
    parentId: '0',
    deptName: '',
    deptCode: '',
    leaderUserId: null,
    phone: null,
    email: null,
    sort: 0,
    status: 0,
    remark: null,
  })
  const parentOptions = computed<TreeSelectOption[]>(() => {
    function collect(nodes: DeptItem[], disabledAncestor = false): TreeSelectOption[] {
      return nodes.flatMap((node) => {
        if (String(node.id) === String(props.initialModel?.id)) return []
        const disabledBranch = disabledAncestor || node.status === 1
        const children = collect(node.children, disabledBranch)
        return [
          {
            key: String(node.id),
            label: node.deptName + (disabledBranch ? `（${dictionary.getDictData('sys_common_status', 1).label}）` : ''),
            disabled: formModel.value.status === 0 && disabledBranch,
            children: children.length ? children : undefined,
          },
        ]
      })
    }
    const options = collect(departments.value)
    const current = props.initialModel
    if (
      current &&
      String(current.parentId) !== '0' &&
      !departments.value.some(function contains(node): boolean {
        return String(node.id) === String(current.parentId) || node.children.some(contains)
      })
    ) {
      options.unshift({ key: String(current.parentId), label: `当前上级（${current.parentId}）` })
    }
    return [{ key: '0', label: '根部门', children: options }]
  })
  const parentValid = computed(() => {
    function includes(options: TreeSelectOption[]): boolean {
      return options.some((option) =>
        option.key === formModel.value.parentId
          ? !option.disabled
          : includes(option.children ?? []),
      )
    }
    return !loading.value && !error.value && includes(parentOptions.value)
  })

  const leaderOptions = computed(() => {
    const current = selectedLeader.value
    return current && !leaderResults.value.some((option) => option.value === current.value)
      ? [current, ...leaderResults.value]
      : leaderResults.value
  })
  const leaderValid = computed(
    () =>
      formModel.value.leaderUserId == null ||
      leaderOptions.value.some(
        (option) => option.value === formModel.value.leaderUserId && !option.disabled,
      ),
  )

  /** 加载真实候选用户；搜索丢弃旧结果，滚动分页保留已选负责人。 */
  async function loadLeaders(append = false) {
    const department = props.initialModel
    if (!department || (append && (leaderLoading.value || !leaderHasMore.value))) return
    const revision = ++leaderRevision
    const pageNum = append ? leaderPage.value + 1 : 1
    leaderLoading.value = true
    leaderError.value = ''
    try {
      const response = await fetchDeptLeaderOptions(department.id, {
        keyword: leaderKeyword.value,
        pageNum,
        pageSize: 20,
      })
      if (revision !== leaderRevision) return
      if (response.code !== 200) throw new Error(response.message)
      const options = response.data.data.map((user) => ({
        value: String(user.id),
        label: `${user.name}（${user.username}）`,
      }))
      leaderResults.value = append ? [...leaderResults.value, ...options] : options
      const selected = options.find((option) => option.value === formModel.value.leaderUserId)
      if (selected) selectedLeader.value = selected
      leaderPage.value = response.data.currentPage
      leaderHasMore.value = response.data.currentPage < response.data.pages
    } catch (reason) {
      if (revision === leaderRevision)
        leaderError.value = reason instanceof Error ? reason.message : '负责人列表加载失败'
    } finally {
      if (revision === leaderRevision) leaderLoading.value = false
    }
  }

  function searchLeaders(keyword: string) {
    clearTimeout(searchTimer)
    leaderRevision++
    leaderKeyword.value = keyword.trim().slice(0, 64)
    leaderResults.value = []
    leaderPage.value = 0
    leaderHasMore.value = false
    searchTimer = setTimeout(() => {
      void loadLeaders()
    }, 250)
  }

  function scrollLeaders(event: Event) {
    const target = event.target as HTMLElement
    if (target.scrollTop + target.clientHeight >= target.scrollHeight - 40) void loadLeaders(true)
  }

  function changeLeader(value: string | null) {
    formModel.value.leaderUserId = value
    selectedLeader.value =
      value == null ? undefined : leaderOptions.value.find((option) => option.value === value)
  }

  watch(
    [() => props.initialModel, () => props.parentDept],
    ([model, parent]) => {
      formModel.value = {
        id: model?.id,
        parentId: String(model?.parentId ?? parent?.id ?? '0'),
        deptName: model?.deptName ?? '',
        deptCode: model?.deptCode ?? '',
        leaderUserId: model?.leaderUserId == null ? null : String(model.leaderUserId),
        phone: model?.phone ?? null,
        email: model?.email ?? null,
        sort: model?.sort ?? 0,
        status: model?.status ?? parent?.status ?? 0,
        remark: model?.remark ?? null,
      }
      formRef.value?.restoreValidation()
      leaderRevision++
      clearTimeout(searchTimer)
      leaderKeyword.value = ''
      leaderResults.value = []
      selectedLeader.value =
        model?.leaderUserId == null
          ? undefined
          : {
              value: String(model.leaderUserId),
              label:
                model.leaderName && model.leaderUsername
                  ? `${model.leaderName}（${model.leaderUsername}）`
                  : '当前负责人已不可选，请清空或重新选择',
              disabled: !model.leaderName || !model.leaderUsername,
            }
      if (model) void loadLeaders()
    },
    { immediate: true },
  )
  onScopeDispose(() => {
    leaderRevision++
    clearTimeout(searchTimer)
  })

  return {
    formRef,
    formModel,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    rules: createDeptRules(parentValid, leaderValid),
    parentOptions,
    loading,
    error,
    reload,
    leaderOptions,
    leaderLoading,
    leaderError,
    loadLeaders,
    searchLeaders,
    scrollLeaders,
    changeLeader,
  }
}
