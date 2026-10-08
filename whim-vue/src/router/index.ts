import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const modules = import.meta.glob('@/views/**/*.vue')
const Layout = () => import('@/layout/index.vue')

/**
 * 路由name需要和组件name一致，否则缓存不起效果
 */
export const constantRouter: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: modules[`/src/views/welcome/index.vue`],
        name: 'home',
        meta: {
          title: '首页',
          isMenu: true,
          icon: 'smile',
        },
      },
      {
        path: '/redirect/:path(.*)',
        redirect: (to) => {
          return {
            path: '/' + to.params.path,
            query: to.query,
            hash: to.hash,
          }
        },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: modules['/src/views/login/index.vue'],
    meta: {
      title: '登录',
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: modules['/src/views/error/404.vue'],
    meta: {
      title: '404',
    },
  },
]
const router = createRouter({
  history: createWebHistory(),
  routes: constantRouter,
})
export default router
