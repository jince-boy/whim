<script setup lang="ts">
import Icon from '@/components/icon/Icon.vue'

interface IconData {
  icon_id: string
  name: string
  font_class: string
  unicode: string
  unicode_decimal: number
}

const props = withDefaults(
  defineProps<{
    iconData: IconData[]
    placeholder?: string
    searchInputPlaceholder?: string
  }>(),
  {
    placeholder: '选择一个图标',
    searchInputPlaceholder: '请输入图标名称或图标类名',
  },
)

const iconValue = defineModel<string | null>({ required: true })

const showPopover = ref(false)

const searchIconClass = ref('')
const debouncedSearch = ref('') // 防抖后的搜索值

let timer: number | null = null
watch(searchIconClass, (val) => {
  if (timer) clearTimeout(timer)
  timer = window.setTimeout(() => {
    debouncedSearch.value = val
  }, 300) // 300ms 防抖
})

const filteredIcons = computed(() => {
  if (!debouncedSearch.value) {
    return props.iconData
  }
  return props.iconData.filter(
    (item) =>
      item.font_class.includes(debouncedSearch.value) || item.name.includes(debouncedSearch.value),
  )
})

const iconButtonClickHandler = (fontClass: string) => {
  iconValue.value = fontClass
  showPopover.value = false
}
</script>

<template>
  <n-popover
    trigger="click"
    :show="showPopover"
    placement="bottom-start"
    :show-arrow="false"
    width="500"
  >
    <template #trigger>
      <n-input-group>
        <n-input-group-label style="width: 40px">
          <Icon :name="iconValue" />
        </n-input-group-label>
        <n-input
          :value="iconValue"
          readonly
          :placeholder="props.placeholder"
          @click="showPopover = !showPopover"
        />
      </n-input-group>
    </template>

    <div>
      <n-input v-model:value="searchIconClass" :placeholder="props.searchInputPlaceholder" />
      <n-scrollbar style="max-height: 300px; margin-top: 12px; padding: 0 12px">
        <n-grid x-gap="12" :cols="8" y-gap="12">
          <n-gi v-for="item in filteredIcons" :key="item.icon_id">
            <n-button @click="iconButtonClickHandler(item.font_class)">
              <Icon :name="item.font_class" />
            </n-button>
          </n-gi>
        </n-grid>
      </n-scrollbar>
    </div>
  </n-popover>
</template>
