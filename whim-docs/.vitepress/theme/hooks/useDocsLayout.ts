import { computed, h, onMounted, onScopeDispose, ref, watch } from 'vue'
import { useData, withBase } from 'vitepress'
import type { MenuOption } from 'naive-ui'
import { navigation, pageLinks } from '../../navigation'

/** 管理导航、移动端菜单、文档快捷键与代码复制。 */
export function useDocsLayout() {
  const { page, frontmatter } = useData()
  const mobileMenuOpen = ref(false)
  const searchOpen = ref(false)
  const copyTimers = new Set<ReturnType<typeof setTimeout>>()
  const activePath = computed(() => `/${page.value.relativePath.replace(/\.md$/, '')}`)
  const currentGroup = computed(() =>
    navigation.find((group) => group.items.some((item) => item.link === activePath.value)),
  )
  const menuOptions: MenuOption[] = navigation.map((group) => ({
    type: 'group',
    label: group.text,
    key: group.text,
    children: group.items.map((item) => ({
      key: item.link,
      label: () => h('a', { href: withBase(item.link) }, item.text),
    })),
  }))
  const adjacentPages = computed(() => {
    const index = pageLinks.findIndex((item) => item.link === activePath.value)
    return { previous: pageLinks[index - 1], next: index >= 0 ? pageLinks[index + 1] : undefined }
  })
  const isHome = computed(() => frontmatter.value.layout === 'home')

  watch(activePath, () => {
    mobileMenuOpen.value = false
    searchOpen.value = false
  })

  /** 使用平台常见快捷键打开本站全文搜索。 */
  function handleShortcut(event: KeyboardEvent) {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k' && !event.isComposing) {
      event.preventDefault()
      searchOpen.value = !searchOpen.value
    }
  }

  /** 复制当前代码块的纯文本，并在按钮上提供操作反馈。 */
  async function copyCode(event: MouseEvent) {
    const target = event.target instanceof Element ? event.target.closest('button.copy') : null
    const code = target?.parentElement?.querySelector('code')?.textContent
    if (!target || !code) return
    try {
      await navigator.clipboard.writeText(code)
      target.classList.add('copied')
    } catch {
      target.classList.add('copy-error')
    }
    const timer = setTimeout(() => {
      target.classList.remove('copied', 'copy-error')
      copyTimers.delete(timer)
    }, 2000)
    copyTimers.add(timer)
  }

  onMounted(() => window.addEventListener('keydown', handleShortcut))
  onScopeDispose(() => {
    if (typeof window !== 'undefined') window.removeEventListener('keydown', handleShortcut)
    copyTimers.forEach(clearTimeout)
  })
  return {
    page,
    isHome,
    activePath,
    currentGroup,
    menuOptions,
    adjacentPages,
    mobileMenuOpen,
    searchOpen,
    copyCode,
  }
}
