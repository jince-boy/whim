import { computed, onScopeDispose, ref, shallowRef, toRaw, toValue } from 'vue'
import type { MaybeRefOrGetter, Ref } from 'vue'
import type { FormInst } from 'naive-ui'
import type { SearchFormOptions, SearchFormRegister, SearchFormRegistration } from '../types'

/** 创建注册式查询表单，默认值、输入草稿和提交快照互相隔离。 */
export function useSearchForm<Values extends object>(
  source: MaybeRefOrGetter<SearchFormOptions<Values>>,
) {
  const options = computed(() => toValue(source))
  const model = ref(structuredClone(toRaw(options.value.defaultValues))) as Ref<Values>
  const instance = shallowRef<SearchFormRegistration>()

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

  const formProps = computed(() => {
    const { defaultValues, grid, searchText, resetText, ...nativeProps } = options.value
    void defaultValues
    void grid
    void searchText
    void resetText
    return {
      labelWidth: 'auto',
      labelPlacement: 'left' as const,
      showFeedback: !!nativeProps.rules,
      ...nativeProps,
      model: model.value,
    }
  })
  const binding = {
    formProps,
    gridProps: computed(() => ({
      cols: '1 s:2 m:3 l:4',
      responsive: 'screen' as const,
      xGap: 16,
      yGap: 0,
      ...options.value.grid,
    })),
    searchText: computed(() => options.value.searchText ?? '搜索'),
    resetText: computed(() => options.value.resetText ?? '重置'),
    resetFields,
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
  return [register, { model, getValues, setValues, resetFields, validate, submit, reset }] as const
}
