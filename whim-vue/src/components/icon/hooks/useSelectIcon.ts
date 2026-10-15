import { computed, h, ref } from 'vue'
import type { Ref } from 'vue'
import type { SelectOption } from 'naive-ui'
import iconfont from '@/assets/iconfont/iconfont.json'
import Icon from '../Icon.vue'

export interface SelectIconProps {
  iconData?: readonly { name: string; font_class: string }[]
  placeholder?: string
  searchInputPlaceholder?: string
}

/** 管理图标选项，复用原生选择器的搜索、键盘操作和表单校验。 */
export function useSelectIcon(props: Readonly<SelectIconProps>, model: Ref<string | null>) {
  const show = ref(false)
  const options = computed(() =>
    (props.iconData ?? iconfont.glyphs).map((icon) => ({
      label: `${icon.name} (${icon.font_class})`,
      value: icon.font_class,
    })),
  )
  const placeholder = computed(() =>
    show.value ? props.searchInputPlaceholder : props.placeholder,
  )

  /** 在选项和当前值中展示图标预览。 */
  const renderLabel = (option: SelectOption) =>
    h('span', { style: { display: 'inline-flex', alignItems: 'center', gap: '8px' } }, [
      h(Icon, { name: String(option.value), size: 18 }),
      String(option.label ?? option.value),
    ])

  /** 只提交单个字符串或空值，由原生选择器触发表单校验。 */
  function updateValue(value: string | number | Array<string | number> | null) {
    if (value !== null && typeof value !== 'string') return
    model.value = value
  }

  return { show, options, placeholder, renderLabel, updateValue }
}
