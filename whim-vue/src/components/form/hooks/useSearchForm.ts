import { computed, onScopeDispose, ref, shallowRef, toRaw, toValue } from 'vue'
import type { MaybeRefOrGetter, Ref } from 'vue'
import type { FormInst } from 'naive-ui'
import type {
  SearchFormBinding,
  SearchFormOptions,
  SearchFormRegister,
  SearchFormRegistration,
} from '../types'

/** 创建注册式查询表单，默认值、输入草稿和提交快照互相隔离。 */
export function useSearchForm<Values extends object>(
  source: MaybeRefOrGetter<SearchFormOptions<Values>>,
) {
  const options = computed(() => toValue(source))
  const model = ref(structuredClone(toRaw(options.value.defaultValues))) as Ref<Values>
  const instance = shallowRef<SearchFormRegistration>()
  const submittedValues = shallowRef(getValues())

  const filters = computed(() =>
    (options.value.fields ?? []).flatMap((field) => {
      const value = submittedValues.value[field.key]
      if (
        value === null ||
        value === undefined ||
        value === '' ||
        (Array.isArray(value) && !value.length)
      )
        return []
      return [
        {
          key: field.key,
          label: field.label,
          value:
            field.format?.(submittedValues.value) ??
            (Array.isArray(value) ? value.join('、') : String(value)),
        },
      ]
    }),
  )

  /** 获取独立快照，修改请求参数不会影响输入框。 */
  function getValues(): Values {
    return structuredClone(toRaw(model.value))
  }

  /** 合并表单值，保持嵌套字段与调用方隔离。 */
  function setValues(values: Partial<Values>) {
    model.value = { ...getValues(), ...structuredClone(toRaw(values)) }
  }

  /** 恢复当前配置的默认值并清理校验，不隐式提交。 */
  function resetFields() {
    model.value = structuredClone(toRaw(options.value.defaultValues))
    instance.value?.restoreValidation()
  }

  /** 提交独立的查询快照，标签、刷新和翻页共用这一份条件。 */
  function commitValues() {
    submittedValues.value = getValues()
  }

  /** 只撤销指定的已提交条件，不提交其他字段尚未搜索的修改。 */
  function removeFilter(key: string) {
    const field = options.value.fields?.find((item) => item.key === key)
    if (!field) return false
    const defaults = structuredClone(toRaw(options.value.defaultValues))
    model.value = { ...getValues(), [field.key]: structuredClone(defaults[field.key]) }
    submittedValues.value = { ...submittedValues.value, [field.key]: defaults[field.key] }
    instance.value?.restoreValidation()
    return true
  }

  const formProps = computed(() => {
    const { title, collapsed, defaultValues, fields, grid, searchText, resetText, ...nativeProps } =
      options.value
    void title
    void collapsed
    void defaultValues
    void fields
    void grid
    void searchText
    void resetText
    return {
      size: 'medium' as const,
      labelPlacement: 'left' as const,
      showFeedback: !!nativeProps.rules,
      ...nativeProps,
      model: model.value,
    }
  })
  const binding: SearchFormBinding = {
    title: computed(() => options.value.title ?? '筛选条件'),
    collapsed: computed(() => options.value.grid?.collapsed ?? options.value.collapsed ?? true),
    filters,
    formProps,
    gridProps: computed(() => ({
      xGap: 10,
      yGap: 10,
      ...options.value.grid,
    })),
    searchText: computed(() => options.value.searchText ?? '搜索'),
    resetText: computed(() => options.value.resetText ?? '重置'),
    resetFields,
    commitValues,
    removeFilter,
  }

  /** 同一 hook 只连接一个表单；重新挂载时可以再次注册。 */
  const register: SearchFormRegister = (target) => {
    if (target && instance.value && instance.value !== target)
      throw new Error('一个 useSearchForm 只能注册一个查询表单')
    instance.value = target
    target?.connect(binding)
  }

  /** 实例操作在挂载后调用，注册缺失时立即报告错误。 */
  function getInstance() {
    if (!instance.value) throw new Error('查询表单尚未注册，请在组件挂载后调用')
    return instance.value
  }

  /** 使用原生校验签名，向调用方保留校验结果。 */
  const validate: FormInst['validate'] = (...args) => getInstance().validate(...args)
  /** 校验后发送 submit 事件，校验失败返回 false。 */
  const submit = () => getInstance().submit()
  /** 恢复默认值并发送 reset 事件。 */
  const reset = () => getInstance().reset()
  onScopeDispose(() => {
    instance.value = undefined
  })
  return [
    register,
    {
      model,
      getValues,
      setValues,
      resetFields,
      validate,
      submit,
      reset,
      commitValues,
      removeFilter,
      submittedValues: computed(() => submittedValues.value),
    },
  ] as const
}
