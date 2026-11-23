<script setup lang="ts">
import { computed } from 'vue'
import { useDictStore } from '@/stores/modules/dict'
import type { ValueType } from '../hooks/types'

const props = defineProps<{ value: ValueType }>()
const dictionary = useDictStore()
const item = computed(() => dictionary.getDictData('sys_config_value_type', props.value))
</script>

<template>
  <n-tag size="small" :bordered="false" :type="item.listClass">{{ item.label }}</n-tag>
</template>
