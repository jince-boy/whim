import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '工作台' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('@/views/notFound/index.vue'),
      meta: { title: '页面不存在' },
    },
  ],
})

/** 根据当前路由同步浏览器标题。 */
router.afterEach((to) => {
  document.title = to.meta.title
    ? `${String(to.meta.title)} · ${import.meta.env.VITE_APP_NAME}`
    : import.meta.env.VITE_APP_TITLE
})

export default router
