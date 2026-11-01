import { onScopeDispose, ref, watch, type ComponentPublicInstance, type Ref } from 'vue'
import type { ScrollbarInst } from 'naive-ui'
import { useTabStore } from '@/stores/modules/tab'

/** 按住并移动标签即可拖动，松手时提交顺序，取消时保留原来的位置。 */
export function useTabDrag(
  tabRoot: Readonly<Ref<ComponentPublicInstance | null>>,
  scrollbar: Readonly<Ref<ScrollbarInst | null>>,
) {
  const tabStore = useTabStore()
  const draggingName = ref('')
  const dragOffset = ref(0)
  const dropTargetName = ref('')
  const dropAfter = ref(false)
  let pointerId: number | null = null
  let root: HTMLElement | null = null
  let pressedName = ''
  let animationFrame = 0
  let previousFrame = 0
  let startX = 0
  let startY = 0
  let pointerX = 0
  let pointerY = 0
  let startListLeft = 0
  let suppressClick = false

  /** 结束手势并清理指针捕获和全局监听。 */
  function finishDrag(commit: boolean) {
    const name = draggingName.value
    const targetName = dropTargetName.value
    const after = dropAfter.value
    const capturedPointer = pointerId
    pointerId = null
    pressedName = ''
    cancelAnimationFrame(animationFrame)
    document.removeEventListener('pointermove', handlePointerMove)
    document.removeEventListener('pointerup', handlePointerUp)
    document.removeEventListener('pointercancel', cancelTabDrag)
    document.removeEventListener('keydown', handleKeyDown)
    document.removeEventListener('visibilitychange', cancelTabDrag)
    window.removeEventListener('blur', cancelTabDrag)
    if (capturedPointer !== null && root?.hasPointerCapture(capturedPointer)) {
      root.releasePointerCapture(capturedPointer)
    }
    root = null
    draggingName.value = ''
    dragOffset.value = 0
    dropTargetName.value = ''
    dropAfter.value = false
    if (commit && name && targetName) tabStore.moveTab(name, targetName, after)
  }

  /** 撤销尚未提交的按下或拖拽。 */
  function cancelTabDrag() {
    finishDrag(false)
  }

  /** 在标签栏内跟随指针，并在边缘滚动时持续计算落点。 */
  function updateDrag(timestamp: number) {
    const viewport = root?.querySelector<HTMLElement>('.tab-scrollbar')
    const list = root?.querySelector<HTMLElement>('.tab-box')
    if (!viewport || !list) {
      cancelTabDrag()
      return
    }
    const bounds = viewport.getBoundingClientRect()
    const inside = pointerY >= bounds.top && pointerY <= bounds.bottom
    const elapsed = previousFrame ? Math.min(timestamp - previousFrame, 32) : 16
    previousFrame = timestamp
    if (inside) {
      const edge = Math.min(36, bounds.width / 3)
      const speed =
        pointerX < bounds.left + edge
          ? -Math.min(1, (bounds.left + edge - pointerX) / edge)
          : pointerX > bounds.right - edge
            ? Math.min(1, (pointerX - bounds.right + edge) / edge)
            : 0
      if (speed) scrollbar.value?.scrollBy({ left: speed * elapsed * 0.5, behavior: 'instant' })
    }
    dragOffset.value = pointerX - startX + startListLeft - list.getBoundingClientRect().left
    if (inside) {
      const candidates = Array.from(
        list.querySelectorAll<HTMLElement>('[data-tab-draggable]'),
      ).filter((element) => element.dataset.tabName !== draggingName.value)
      const target = candidates.find((element) => {
        const rect = element.getBoundingClientRect()
        return pointerX < rect.left + rect.width / 2
      })
      dropTargetName.value = (target ?? candidates.at(-1))?.dataset.tabName ?? ''
      dropAfter.value = !target
    } else {
      dropTargetName.value = ''
    }
    animationFrame = requestAnimationFrame(updateDrag)
  }

  /** 移动超过轻微抖动距离即开始拖拽，不设置按住等待时间。 */
  function handlePointerMove(event: PointerEvent) {
    if (event.pointerId !== pointerId) return
    pointerX = event.clientX
    pointerY = event.clientY
    if (!draggingName.value) {
      if (Math.hypot(pointerX - startX, pointerY - startY) <= 4) return
      draggingName.value = pressedName
      suppressClick = true
      root?.setPointerCapture(event.pointerId)
      window.getSelection()?.removeAllRanges()
      previousFrame = 0
      animationFrame = requestAnimationFrame(updateDrag)
    }
    event.preventDefault()
  }

  /** 仅在标签栏内松手时提交落点。 */
  function handlePointerUp(event: PointerEvent) {
    if (event.pointerId !== pointerId) return
    if (draggingName.value) {
      pointerX = event.clientX
      pointerY = event.clientY
      cancelAnimationFrame(animationFrame)
      updateDrag(performance.now())
    }
    finishDrag(true)
  }

  /** 按 Esc 放弃本次拖动。 */
  function handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape') cancelTabDrag()
  }

  /** 仅响应标签栏自身丢失捕获，忽略触屏从子按钮转移捕获的事件。 */
  function handleLostPointerCapture(event: PointerEvent) {
    if (event.target === root && event.pointerId === pointerId) cancelTabDrag()
  }

  /** 从可移动标签的主体记录按下位置，未拖动时仍按普通点击处理。 */
  function handleTabPointerDown(event: PointerEvent) {
    if (!event.isPrimary || event.button !== 0 || pointerId !== null) return
    suppressClick = false
    const target = event.target as Element
    const element = target.closest<HTMLElement>('[data-tab-draggable]')
    if (!element || target.closest('.tab-close, .button-tab-close') || tabStore.closing) return
    root = tabRoot.value?.$el as HTMLElement
    const list = root.querySelector<HTMLElement>('.tab-box')
    if (!list) return
    pressedName = element.dataset.tabName ?? ''
    startListLeft = list.getBoundingClientRect().left
    pointerId = event.pointerId
    startX = pointerX = event.clientX
    startY = pointerY = event.clientY
    document.addEventListener('pointermove', handlePointerMove, { passive: false })
    document.addEventListener('pointerup', handlePointerUp)
    document.addEventListener('pointercancel', cancelTabDrag)
    document.addEventListener('keydown', handleKeyDown)
    document.addEventListener('visibilitychange', cancelTabDrag)
    window.addEventListener('blur', cancelTabDrag)
  }

  /** 拦截拖拽松手生成的点击，键盘激活标签仍正常生效。 */
  function handleDragClick(event: MouseEvent) {
    if (!suppressClick || event.detail === 0) return
    event.preventDefault()
    event.stopPropagation()
    suppressClick = false
  }

  /** 拖动期间阻止系统菜单，普通右键先取消待拖动的按下状态。 */
  function handleDragContextMenu(event: MouseEvent) {
    if (draggingName.value) {
      event.preventDefault()
      event.stopPropagation()
      return
    }
    cancelTabDrag()
  }

  watch(
    [() => tabStore.tabs.map((tab) => tab.name).join('\0'), () => tabStore.closing],
    cancelTabDrag,
  )
  onScopeDispose(cancelTabDrag)

  return {
    draggingName,
    dragOffset,
    dropTargetName,
    dropAfter,
    handleTabPointerDown,
    handleLostPointerCapture,
    handleDragClick,
    handleDragContextMenu,
    cancelTabDrag,
  }
}
