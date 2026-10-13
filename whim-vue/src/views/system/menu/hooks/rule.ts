import type { Ref } from 'vue'
import type { FormRules } from 'naive-ui'
import type { MenuSave } from './types'
/** 校验只约束当前类型的字段，后端再次核验路径、唯一性和父子关系。 */
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
          /^[a-z][A-Za-z0-9]*(?::[a-z][A-Za-z0-9]*){2,}$/.test(model.value.perms || ''),
        message: '请输入精确权限标识，例如system:menu:list',
        trigger: 'blur',
      },
    ],
    path: [
      {
        validator: () =>
          model.value.menuType === 3 ||
          (model.value.menuType === 4
            ? /^https?:\/\/[^\s]+$/.test(model.value.path || '')
            : /^\/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*)(?:\/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*))*$/.test(
                model.value.path || '',
              )),
        message: '请输入小驼峰绝对路由路径，外链须使用HTTP或HTTPS',
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
