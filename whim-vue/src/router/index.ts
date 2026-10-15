import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

/** 固定路由只承载布局、账号入口和异常页，业务页面全部由后端授权菜单注册。 */
export const constantRouter: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: '/redirect/:path(.*)',
        redirect: (to) => ({ path: '/' + to.params.path, query: to.query, hash: to.hash }),
        meta: { isTab: false },
      },
      {
        path: 'profile',
        component: () => import('@/views/profile/index.vue'),
        name: 'UserProfile',
        meta: {
          title: '个人信息设置',
          breadcrumbs: [{ title: '个人信息设置', path: '/profile', name: 'UserProfile' }],
          icon: 'yonghu',
          isTab: true,
        },
      },
      {
        path: 'noAccess',
        name: 'NoAccess',
        component: () => import('@/views/error/noAccess/index.vue'),
        meta: { title: '暂无可访问页面', isTab: false },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', isTab: false },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', isTab: false },
  },
]
const router = createRouter({
  history: createWebHistory(),
  routes: constantRouter,
})
export default router
