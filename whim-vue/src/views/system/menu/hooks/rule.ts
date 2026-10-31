import type { Ref } from 'vue'
import type { FormRules } from 'naive-ui'
import { MenuType, type MenuSave } from './types'

/** 校验当前菜单类型的输入及父级选择，唯一性和并发修改由后端校验。 */
export function createMenuRules(
  model: Ref<MenuSave>,
  parentValid: Readonly<Ref<boolean>>,
): FormRules {
  return {
    menuName: [
      { required: true, whitespace: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 64, message: '菜单名称不能超过64个字符', trigger: 'blur' },
    ],
    menuType: [
      {
        type: 'enum',
        enum: [MenuType.DIRECTORY, MenuType.MENU, MenuType.BUTTON, MenuType.EXTERNAL],
        required: true,
        message: '请选择菜单类型',
        trigger: 'change',
      },
    ],
    parentId: [
      {
        required: true,
        validator: () => parentValid.value,
        message: '请选择类型及启用状态匹配的上级菜单',
        trigger: 'change',
      },
    ],
    routeName: [
      {
        required: true,
        validator: () =>
          (model.value.menuType !== MenuType.DIRECTORY && model.value.menuType !== MenuType.MENU) ||
          /^[A-Za-z][A-Za-z0-9]{0,127}$/.test(model.value.routeName || ''),
        message: '路由名称须以字母开头，仅含字母和数字，最多128个字符',
        trigger: 'blur',
      },
    ],
    perms: [
      {
        validator: () =>
          !model.value.perms?.trim() ||
          (model.value.perms.length <= 128 &&
            /^[a-z][A-Za-z0-9]*(?::[a-z][A-Za-z0-9]*){2,}$/.test(model.value.perms.trim())),
        message: '权限标识最多128个字符，例如 system:menu:list，可留空',
        trigger: 'blur',
      },
    ],
    path: [
      {
        required: true,
        validator: () => {
          if (model.value.menuType === MenuType.BUTTON) return true
          const path = model.value.path?.trim()
          if (!path || path.length > 255) return false
          if (model.value.menuType !== MenuType.EXTERNAL) {
            return /^\/(?:[A-Za-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*)(?:\/(?:[A-Za-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*))*$/.test(
              path,
            )
          }
          try {
            const address = new URL(path)
            return (
              ['http:', 'https:'].includes(address.protocol) &&
              !address.username &&
              !address.password
            )
          } catch {
            return false
          }
        },
        message:
          '路由须以 / 开头，路径段使用字母数字或 :参数名；外链须为有效的 HTTP 或 HTTPS 地址，最多255个字符',
        trigger: 'blur',
      },
    ],
    component: [
      {
        required: true,
        validator: () => model.value.menuType !== MenuType.MENU || !!model.value.component,
        message: '请选择页面组件',
        trigger: 'change',
      },
    ],
    sort: [
      {
        type: 'integer',
        required: true,
        min: 0,
        max: 2147483647,
        message: '排序须为0至2147483647之间的整数',
        trigger: ['blur', 'change'],
      },
    ],
    param: [{ max: 255, message: '路由参数不能超过255个字符', trigger: 'blur' }],
    remark: [{ max: 255, message: '备注不能超过255个字符', trigger: 'blur' }],
  }
}
