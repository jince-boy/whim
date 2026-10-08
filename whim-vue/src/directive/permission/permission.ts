import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/modules/user.ts'

/**
 * 处理权限验证
 */
const handlePermission = (el: HTMLElement, binding: DirectiveBinding<string[] | undefined | null>): void => {
  const userStore = useUserStore()
  const { value } = binding

  // 如果值为 undefined 或 null，则不进行权限验证，直接显示元素
  if (value === undefined || value === null) {
    return
  }

  // 如果是空数组，抛出错误
  if (!value.length) {
    throw new Error(
      "v-permission 指令需要指定权限值，例如 v-permission=\"['system:user:add','system:user:update']\"",
    )
  }

  const hasPermission = userStore.permissionCode.some((permission) => {
    return permission === '*' || value.includes(permission)
  })

  if (!hasPermission) {
    removeElement(el)
  }
}

/**
 * 处理角色验证
 */
const handleRole = (el: HTMLElement, binding: DirectiveBinding<string[] | undefined | null>): void => {
  const userStore = useUserStore()
  const { value } = binding

  // 如果值为 undefined 或 null，则不进行角色验证，直接显示元素
  if (value === undefined || value === null) {
    return
  }

  // 如果是空数组，抛出错误
  if (!value.length) {
    throw new Error("v-role 指令需要指定角色值，例如 v-role=\"['admin','editor']\"")
  }

  const hasRole = userStore.roleCode.some((role) => {
    return role === '*' || value.includes(role)
  })

  if (!hasRole) {
    removeElement(el)
  }
}

/**
 * 安全移除元素
 */
const removeElement = (el: HTMLElement): void => {
  if (el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

/**
 * 权限指令
 */
export const permissionDirective: Directive<HTMLElement, string[] | undefined | null> = {
  mounted: handlePermission,
  updated: handlePermission,
}

/**
 * 角色指令
 */
export const roleDirective: Directive<HTMLElement, string[] | undefined | null> = {
  mounted: handleRole,
  updated: handleRole,
}
