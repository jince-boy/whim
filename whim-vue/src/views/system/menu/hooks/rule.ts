import type { Ref } from 'vue'
import type { FormRules } from 'naive-ui'
import type { MenuSave } from './types'
/** 前端校验输入格式，权限标识可选；后端维护唯一性和父子关系。 */
export function createMenuRules(model: Ref<MenuSave>): FormRules {
  return {
    menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur', max: 64 }],
    parentId: [{ required: true, message: '请选择上级菜单', trigger: 'change' }],
    routeName: [
      {
        validator: () =>
          ![1, 2].includes(model.value.menuType) ||
          /^[A-Za-z][A-Za-z0-9]*$/.test(model.value.routeName || ''),
        message: '请输入唯一的字母数字路由名称',
        trigger: 'blur',
      },
    ],
    perms: [
      {
        validator: () =>
          ![2, 3].includes(model.value.menuType) ||
          !model.value.perms?.trim() ||
          /^[a-z][A-Za-z0-9]*(?::[a-z][A-Za-z0-9]*){2,}$/.test(model.value.perms.trim()),
        message: '权限标识可留空，填写时例如system:menu:list',
        trigger: 'blur',
      },
    ],
    path: [
      {
        validator: () => {
          if (model.value.menuType === 3) return true
          const path = model.value.path?.trim()
          if (!path) return false
          if (model.value.menuType !== 4) return true
          try {
            const address = new URL(path)
            return (
              ['http:', 'https:'].includes(address.protocol) && !address.username && !address.password
            )
          } catch {
            return false
          }
        },
        message: '请输入路由地址，外链须使用有效的HTTP或HTTPS地址',
        trigger: 'blur',
      },
    ],
    component: [
      {
        validator: () => model.value.menuType !== 2 || !!model.value.component,
        message: '请选择页面组件',
        trigger: 'change',
      },
    ],
  }
}
