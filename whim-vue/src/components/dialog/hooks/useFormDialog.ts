import {
  h,
  normalizeStyle,
  onDeactivated,
  onScopeDispose,
  readonly,
  ref,
  shallowRef,
  toRaw,
} from 'vue'
import { useModal, type ModalOptions, type ModalReactive } from 'naive-ui'
import { message } from '@/plugins'
import { isAxiosError } from 'axios'
import type {
  ConfirmDialogOptions,
  DialogController,
  DialogFormComponent,
  DialogOptions,
  DialogSubmitResult,
  FormDialogOptions,
} from '../types'

/** 管理表单与确认弹窗，统一提交、关闭和所属页面的生命周期。 */
export function useFormDialog() {
  const modal = useModal()
  const activeDialogs = new Set<() => void>()
  let disposed = false

  /** HTTP 错误已由项目请求拦截器提示，其余异常在此展示。 */
  function reportError(error: unknown, fallback: string) {
    if (!isAxiosError(error)) message.error(error instanceof Error ? error.message : fallback)
  }

  /** 两类弹窗共用提交锁，所有关闭入口遵循同一状态。 */
  function openDialog(
    options: DialogOptions,
    presentation: ModalOptions,
    onSubmit: () => Promise<DialogSubmitResult>,
  ): DialogController {
    if (disposed) throw new Error('页面已卸载，无法打开弹窗')
    const submitting = ref(false)
    let closed = false
    const nativeProps = options.modalProps ?? {}
    const idleProps = {
      closable: nativeProps.closable ?? true,
      closeOnEsc: nativeProps.closeOnEsc ?? true,
      maskClosable: nativeProps.maskClosable ?? false,
      negativeButtonProps: nativeProps.negativeButtonProps,
      positiveButtonProps: nativeProps.positiveButtonProps,
    }

    /** 强制清理只用于成功提交及页面销毁，异步结果不会重新操作弹窗。 */
    function destroy() {
      if (closed) return
      closed = true
      activeDialogs.delete(destroy)
      instance.destroy()
    }

    /** 用户或业务主动关闭时不能中断正在提交的操作。 */
    function close() {
      if (closed || submitting.value) return false
      destroy()
      return true
    }

    /** 等待业务结果，失败保留草稿，成功关闭后再执行刷新。 */
    async function submit(): Promise<boolean> {
      if (closed || submitting.value) return false
      submitting.value = true
      Object.assign(instance, {
        loading: true,
        closable: false,
        closeOnEsc: false,
        maskClosable: false,
        negativeButtonProps: { ...idleProps.negativeButtonProps, disabled: true },
        positiveButtonProps: { ...idleProps.positiveButtonProps, disabled: true, loading: true },
      })
      try {
        const result = await onSubmit()
        if (closed || disposed || result === false) return false
        if (result && typeof result === 'object' && result.code !== 200) {
          message.error(result.message || '操作失败，请稍后重试')
          return false
        }
        destroy()
        if (options.successMessage !== false) {
          message.success(
            options.successMessage ||
              (result && typeof result === 'object' ? result.message : '') ||
              '操作成功',
          )
        }
        // 保存与刷新分开处理，避免刷新失败后让用户重复保存。
        try {
          await options.onSuccess?.()
        } catch (error) {
          if (!disposed) reportError(error, '操作已成功，但刷新失败')
        }
        return true
      } catch (error) {
        if (!closed && !disposed) reportError(error, '操作失败，请稍后重试')
        return false
      } finally {
        submitting.value = false
        if (!closed) Object.assign(instance, idleProps, { loading: false })
      }
    }

    const instance: ModalReactive = modal.create({
      positiveText: '确定',
      negativeText: '取消',
      draggable: true,
      ...nativeProps,
      ...presentation,
      ...idleProps,
      preset: 'dialog',
      to: nativeProps.to ?? (document.fullscreenElement as HTMLElement | null) ?? undefined,
      style: normalizeStyle([
        {
          width:
            typeof options.width === 'number' ? `${options.width}px` : (options.width ?? '600px'),
          maxWidth: 'calc(100vw - 32px)',
          maxHeight: 'calc(100dvh - 32px)',
          overflowY: 'auto',
        },
        nativeProps.style,
      ]),
      /** 由统一入口关闭，阻止原生按钮再次自动关闭。 */
      onPositiveClick: async () => {
        await submit()
        return false
      },
      /** 取消按钮与命令式关闭遵循相同的提交锁。 */
      onNegativeClick: () => {
        close()
        return false
      },
      /** 关闭图标与命令式关闭遵循相同的提交锁。 */
      onClose: () => {
        close()
        return false
      },
      /** 原生遮罩和 Esc 关闭时立即标记，避免过渡期间再次提交。 */
      onUpdateShow: (show) => {
        if (!show) destroy()
      },
      /** 清理实例并保留原生离场通知。 */
      onAfterLeave: () => {
        activeDialogs.delete(destroy)
        closed = true
        nativeProps.onAfterLeave?.()
      },
    })
    activeDialogs.add(destroy)
    return { submitting: readonly(submitting), submit, close }
  }

  /** 从表单组件推导 props 和提交模型，无需手写泛型或关闭弹窗。 */
  function openFormDialog<C extends DialogFormComponent>(options: FormDialogOptions<C>) {
    const formInstance = shallowRef<InstanceType<C> | null>(null)
    return openDialog(
      options,
      {
        title: options.title,
        showIcon: options.modalProps?.showIcon ?? false,
        content: () => h(options.formComponent, { ...options.formProps, ref: formInstance }),
      },
      async () => {
        const form = formInstance.value
        if (!form?.formRef) throw new Error('表单尚未挂载，请稍后重试')
        try {
          await form.formRef.validate()
        } catch (error) {
          // Naive UI 校验错误由字段展示，其他异常交给统一错误处理。
          if (Array.isArray(error)) return false
          throw error
        }
        const model = structuredClone(toRaw(form.formModel))
        return options.onSubmit(model)
      },
    )
  }

  /** 通用确认支持原生类型、按钮文字和内容渲染。 */
  function openConfirmDialog(options: ConfirmDialogOptions) {
    return openDialog(
      options,
      { title: options.title ?? '操作确认', content: options.content },
      async () => options.onSubmit(),
    )
  }

  /** 删除只是通用确认的警告预设，复用同一提交保护。 */
  function openDeleteDialog(options: ConfirmDialogOptions) {
    return openConfirmDialog({
      ...options,
      title: options.title ?? '删除确认',
      successMessage: options.successMessage ?? '删除成功',
      modalProps: { type: 'warning', ...options.modalProps },
    })
  }

  /** 离开缓存页面或卸载时只清理当前 hook 创建的弹窗。 */
  function closeOwnedDialogs() {
    for (const destroy of activeDialogs) destroy()
  }
  onDeactivated(closeOwnedDialogs)
  onScopeDispose(() => {
    disposed = true
    closeOwnedDialogs()
  })
  return { openFormDialog, openConfirmDialog, openDeleteDialog }
}
