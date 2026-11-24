import type { FormRules } from 'naive-ui'
import { getRichText, hasRichImage } from '@/components/richTextEditor/richTextHtml'

export const createNoticeRules = (isBusy: () => boolean): FormRules => ({
  title: [
    {
      required: true,
      whitespace: true,
      max: 100,
      message: '请输入 1 至 100 个字符的标题',
      trigger: 'blur',
    },
  ],
  contentHtml: [
    {
      validator: (_, html: string) => {
        if (isBusy()) return new Error('请等待正文图片处理完成')
        const text = getRichText(html)
        if (!text.trim() && !hasRichImage(html)) return new Error('请输入公告正文或上传图片')
        if (text.length > 10000) return new Error('公告正文不能超过10000个字符')
        if (new TextEncoder().encode(JSON.stringify(html)).length > 256 * 1024)
          return new Error('公告排版内容过大，请简化格式')
        return true
      },
    },
  ],
  noticeType: [{ required: true, type: 'number', message: '请选择公告类型', trigger: 'change' }],
  status: [{ required: true, type: 'number', message: '请选择公告状态', trigger: 'change' }],
})
