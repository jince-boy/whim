# 图片裁剪

`WhimImageCropper` 使用 Cropper.js 2，提供固定比例裁剪框、拖动图片、滚轮或按钮缩放、重置及图片导出。组件按需加载裁剪依赖，不发送上传请求；调用方在确认后自行保存文件。

```vue
<script setup lang="ts">
import { ref } from 'vue'
import WhimImageCropper from '@/components/imageCropper/WhimImageCropper.vue'

const cropVisible = ref(false)
const sourceFile = ref<File | null>(null)
const croppedFile = ref<File | null>(null)

function openCropper(file: File) {
  sourceFile.value = file
  cropVisible.value = true
}

function acceptCrop(file: File) {
  croppedFile.value = file
}
</script>

<template>
  <WhimImageCropper
    v-model:show="cropVisible"
    :file="sourceFile"
    title="裁剪封面"
    :output-width="1200"
    :output-height="675"
    output-type="image/jpeg"
    @confirm="acceptCrop"
  />
</template>
```

文件选择和业务校验由调用方处理，选图后调用 `openCropper(file)`。`confirm` 返回完整裁剪结果 `File`；取消、关闭或导出失败均不触发确认，也不会修改传入的文件。

| 属性／事件 | 说明 | 默认值 |
| --- | --- | --- |
| `v-model:show` | 弹窗显示状态 | `false` |
| `file` | 待裁剪图片，类型为 `File \| null` | 必填 |
| `title` | 弹窗标题 | 裁剪图片 |
| `output-width` | 导出宽度，1～2048 的整数 | 512 |
| `output-height` | 导出高度，1～2048 的整数 | 512 |
| `output-type` | `image/png`、`image/jpeg` 或 `image/webp` | `image/png` |
| `quality` | JPEG／WebP 编码质量，0～1 | 0.92 |
| `confirm` | 成功导出后的 `File` | — |

裁剪框比例由导出宽高决定。默认导出 512×512 的正方形 PNG；JPEG 使用白色背景。GIF 裁剪后变成静态图片。原图最多2400万像素、单边不超过16384像素；关闭、替换图片、页面停用或卸载时会销毁裁剪器并释放临时地址。
