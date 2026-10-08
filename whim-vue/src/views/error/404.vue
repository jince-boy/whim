<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({
  name: 'NotFound',
})

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <n-el class="not-found-container">
    <div class="content">
      <div class="error-code">404</div>
      <h1 class="title">页面未找到</h1>
      <p class="message">您访问的页面不存在或已被移除</p>
      <button @click="goHome" class="home-button">返回首页</button>
    </div>
  </n-el>
</template>

<style scoped lang="scss">
.not-found-container {
  height: 100vh;
  //background-color: #33cc99;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Arial', sans-serif;
  //color: white;
  padding: 2rem;

  .content {
    text-align: center;

    .error-code {
      font-size: 8rem;
      font-weight: 900;
      line-height: 1;
      margin-bottom: 1rem;
      text-shadow: 3px 3px 0 rgba(0, 0, 0, 0.1);
    }

    .title {
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }

    .message {
      font-size: 1.2rem;
      margin-bottom: 2rem;
      opacity: 0.9;
    }

    .home-button {
      border: none;
      padding: 6px 12px;
      font-weight: bold;
      border-radius: 50px;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}
</style>
