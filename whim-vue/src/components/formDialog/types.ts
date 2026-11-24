import type { FormInst, ModalOptions } from 'naive-ui'
import type { AllowedComponentProps, Component, ComponentCustomProps, Ref, VNodeProps } from 'vue'
import type { ApiResponse } from '@/api'

/** 返回接口结果时自动判断业务状态；false 保留弹窗，void/true 表示成功。 */
export type DialogSubmitResult = ApiResponse | boolean | void

/** 表单组件通过 defineExpose 提供校验实例与编辑草稿。 */
export type DialogFormComponent = Component &
  (new (...args: never[]) => {
    $props: object
    formRef: FormInst | null
    formModel: object
  })

export type DialogFormProps<C extends DialogFormComponent> = Omit<
  InstanceType<C>['$props'],
  keyof VNodeProps | keyof AllowedComponentProps | keyof ComponentCustomProps
>

/** 原生外观配置保留补全，提交流程和内容由封装维护。 */
export type DialogModalProps = Omit<
  ModalOptions,
  | 'show'
  | 'loading'
  | 'preset'
  | 'title'
  | 'content'
  | 'render'
  | 'action'
  | 'onPositiveClick'
  | 'onNegativeClick'
  | 'onClose'
  | 'onUpdateShow'
  | 'onUpdate:show'
>

export interface DialogOptions {
  title?: string
  width?: number | string
  modalProps?: DialogModalProps
  /** 默认使用接口消息或操作提示；false 关闭成功提示。 */
  successMessage?: string | false
  /** 保存成功且弹窗关闭后执行；刷新失败不会重新开放提交。 */
  onSuccess?: () => unknown | Promise<unknown>
}

export type FormDialogOptions<C extends DialogFormComponent> = DialogOptions & {
  title: string
  formComponent: C
  onSubmit: (
    model: InstanceType<C>['formModel'],
  ) => DialogSubmitResult | Promise<DialogSubmitResult>
} & (Record<string, never> extends DialogFormProps<C>
    ? { formProps?: DialogFormProps<C> }
    : { formProps: DialogFormProps<C> })

export interface ConfirmDialogOptions extends DialogOptions {
  content: NonNullable<ModalOptions['content']>
  onSubmit: () => DialogSubmitResult | Promise<DialogSubmitResult>
}

export interface DialogController {
  submitting: Readonly<Ref<boolean>>
  /** 走同一校验和提交入口，重复调用返回 false。 */
  submit: () => Promise<boolean>
  /** 提交中禁止关闭；已经关闭时返回 false。 */
  close: () => boolean
}
