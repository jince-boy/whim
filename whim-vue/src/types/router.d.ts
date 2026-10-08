import 'vue-router'

// 为了确保这个文件被当作一个模块，添加至少一个 `export` 声明
export {}

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    keepAlive?: boolean
    isMenu?: boolean
    isShow?: boolean
    isExternal?: boolean
    link?: string
    icon?: string
    visible?: 0 | 1
  }
}
