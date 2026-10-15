import { ref } from 'vue'
import { useRouter } from 'vue-router'

/** 默认首页组件；首页归属及登录落点不在此页面判断。 */
export function useDefaultHome() {
  const router = useRouter()
  const count = ref(0)
  /** 保留现有演示页的导航行为。 */
  function toggleRoute(): void {
    void router.push('/system/user')
  }
  return { count, toggleRoute }
}
