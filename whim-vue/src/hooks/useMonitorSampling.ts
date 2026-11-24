import {
  computed,
  onActivated,
  onDeactivated,
  onMounted,
  onUnmounted,
  ref,
  shallowRef,
  watch,
} from 'vue'
import type { ApiResponse } from '@/api'
import { useUserStore } from '@/stores/modules/user'

export const monitorSampleInterval = 5000
const historyWindow = 5 * 60 * 1000
const maxSamples = 120
type MonitorSample = { sampledAt: number; instanceId?: string }

/** 同一时间的采样只保留一份，同时限制历史时长和条数。 */
function appendSample<T extends MonitorSample>(samples: T[], sample: T, limit: number) {
  const cutoff = sample.sampledAt - historyWindow
  const recent = samples.filter(item => item.sampledAt >= cutoff && item.sampledAt < sample.sampledAt)
  return [...recent, sample].slice(-limit)
}

/** 在有权限且页面可见时轮询，手动刷新和自动采样共用同一个请求生命周期。 */
export function useMonitorSampling<T extends MonitorSample, H extends MonitorSample = T>(
  permission: string,
  request: (signal: AbortSignal) => Promise<ApiResponse<T>>,
  toHistory?: (sample: T) => H,
) {
  const user = useUserStore()
  const canView = computed(() => user.hasPermission(permission))
  const active = ref(false)
  const visible = ref(!document.hidden)
  const refreshKey = ref(0)
  const loading = ref(false)
  const failed = ref(false)
  const samples = shallowRef<T[]>([])
  const history = shallowRef<H[]>([])

  function refresh() {
    if (!loading.value) refreshKey.value++
  }

  watch(
    [canView, active, visible, refreshKey],
    async ([allowed, pageActive, tabVisible], _, onCleanup) => {
      if (!allowed) {
        samples.value = []
        history.value = []
        failed.value = false
      }
      loading.value = allowed && pageActive && tabVisible
      if (!loading.value) return

      const controller = new AbortController()
      let timer: number | undefined
      // 页面隐藏、权限收回和卸载都会结束本轮采样，旧响应不能覆盖新数据。
      onCleanup(() => {
        controller.abort()
        window.clearTimeout(timer)
      })
      try {
        const { code, data } = await request(controller.signal)
        if (controller.signal.aborted) return
        failed.value = code !== 200
        if (failed.value) return
        // 切换后端实例时从新时间线开始，避免把不同机器的指标连成趋势。
        const sameInstance = samples.value.at(-1)?.instanceId === data.instanceId
        if (toHistory) {
          history.value = appendSample(sameInstance ? history.value : [], toHistory(data), maxSamples)
        }
        // 趋势有独立读数时，只留相邻两份完整快照用于计算速率。
        samples.value = appendSample(sameInstance ? samples.value : [], data, toHistory ? 2 : maxSamples)
        timer = window.setTimeout(refresh, monitorSampleInterval)
      } catch {
        // 采集失败时保留上次成功读数，等待手动重试。
        if (!controller.signal.aborted) failed.value = true
      } finally {
        if (!controller.signal.aborted) loading.value = false
      }
    },
    { flush: 'sync' },
  )

  function syncVisibility() {
    visible.value = !document.hidden
  }
  onMounted(() => {
    document.addEventListener('visibilitychange', syncVisibility)
    active.value = true
  })
  onUnmounted(() => document.removeEventListener('visibilitychange', syncVisibility))
  onActivated(() => {
    active.value = true
  })
  onDeactivated(() => {
    active.value = false
  })
  return { canView, samples, history, loading, failed, refresh }
}
