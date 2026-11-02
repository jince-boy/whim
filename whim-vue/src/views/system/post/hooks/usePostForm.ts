import { useDictStore } from '@/stores/modules/dict'
import { computed, ref, useTemplateRef, watch } from 'vue'
import type { FormInst } from 'naive-ui'
import { useDeptOptions } from '@/views/system/dept/hooks/useDeptOptions'
import { postRules } from './rule'
import type { PostFormProps, PostSave } from './types'

/** 岗位编辑草稿与部门选项，部门为空表示全组织岗位。 */
export function usePostForm(props: PostFormProps) {
  const dictionary = useDictStore()
  const formRef = useTemplateRef<FormInst>('formRef')
  const formModel = ref<PostSave>({
    deptId: null,
    postName: '',
    postCode: '',
    sort: 0,
    status: 0,
    remark: null,
  })
  const { selection, loading, error, canLoad, reload } = useDeptOptions(
    () => formModel.value.status === 0,
  )
  const departmentOptions = computed(() => {
    const current = props.initialModel
    // 超出部门查询范围或已停用的当前归属仍可回显，不能静默改成全组织岗位。
    if (current?.deptId != null && !selection.value.names.has(String(current.deptId))) {
      return [
        { key: String(current.deptId), label: `当前部门（${current.deptId}）` },
        ...selection.value.options,
      ]
    }
    return selection.value.options
  })

  watch(
    () => props.initialModel,
    (model) => {
      formModel.value = {
        id: model?.id,
        deptId: model?.deptId == null ? null : String(model.deptId),
        postName: model?.postName ?? '',
        postCode: model?.postCode ?? '',
        sort: model?.sort ?? 0,
        status: model?.status ?? 0,
        remark: model?.remark ?? null,
      }
      formRef.value?.restoreValidation()
    },
    { immediate: true },
  )

  return {
    formRef,
    formModel,
    statusOptions: computed(() => dictionary.getDictNumberOptions('sys_common_status')),
    rules: postRules,
    departmentOptions,
    loading,
    error,
    canLoadDepartments: canLoad,
    reload,
  }
}
