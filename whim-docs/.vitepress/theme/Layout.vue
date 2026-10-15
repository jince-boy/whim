<script setup lang="ts">
import { defineAsyncComponent, inject, useSSRContext } from 'vue'
import { Content, withBase } from 'vitepress'
import {
  NAnchor,
  NAnchorLink,
  NButton,
  NConfigProvider,
  NDrawer,
  NDrawerContent,
  NMenu,
  NMessageProvider,
  NModalProvider,
  NScrollbar,
  NSwitch,
  zhCN,
  dateZhCN,
} from 'naive-ui'
import { collectStylesKey } from './types'
import { useDocsTheme } from './naive-theme'
import { useDocsLayout } from './hooks/useDocsLayout'
import HomePage from './components/HomePage.vue'

const SearchDialog = defineAsyncComponent(() => import('./components/SearchDialog.vue'))

const { theme, overrides, isDark } = useDocsTheme()
const {
  page,
  isHome,
  activePath,
  currentGroup,
  menuOptions,
  adjacentPages,
  mobileMenuOpen,
  searchOpen,
  copyCode,
} = useDocsLayout()

if (import.meta.env.SSR) {
  const context = useSSRContext()
  if (context) {
    context.collectNaiveStyles = inject(collectStylesKey)
    context.docsRelativePath = page.value.relativePath
  }
}
</script>

<template>
  <NConfigProvider
    :theme="theme"
    :theme-overrides="overrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <NMessageProvider>
      <NModalProvider>
        <a class="skip-link" href="#main-content">跳转到正文</a>
        <header class="site-header">
          <div class="header-inner">
            <NButton
              class="mobile-menu-button"
              quaternary
              aria-label="打开文档目录"
              @click="mobileMenuOpen = true"
              >☰</NButton
            >
            <a class="site-brand" :href="withBase('/')" aria-label="Whim 文档首页">
              <span class="brand-mark" aria-hidden="true">W</span>
              <span>Whim <span class="brand-caption">/ 开发文档</span></span>
            </a>
            <nav class="header-navigation" aria-label="主要导航">
              <a
                :href="withBase('/guide/introduction')"
                :class="{ active: activePath.startsWith('/guide/') }"
                >指南</a
              >
              <a
                :href="withBase('/components/overview')"
                :class="{ active: activePath.startsWith('/components/') }"
                >组件</a
              >
              <a
                :href="withBase('/backend/architecture')"
                :class="{ active: activePath.startsWith('/backend/') }"
                >后端</a
              >
            </nav>
            <NButton
              class="search-trigger"
              secondary
              aria-label="搜索文档，快捷键 Ctrl 或 Command 加 K"
              @click="searchOpen = true"
            >
              <span>搜索文档</span><kbd>Ctrl K</kbd>
            </NButton>
            <NSwitch
              v-model:value="isDark"
              aria-label="切换深色主题"
              :title="isDark ? '切换到浅色主题' : '切换到深色主题'"
              size="small"
            />
          </div>
        </header>

        <HomePage v-if="isHome" id="main-content" />
        <div v-else class="docs-layout">
          <aside class="sidebar" aria-label="文档目录">
            <NScrollbar>
              <div class="sidebar-heading">开发手册 <span>WHIM</span></div>
              <NMenu :value="activePath" :options="menuOptions" :indent="16" />
              <div class="sidebar-note">基于仓库源码维护<br />Vue 3 · Naive UI · Spring Boot</div>
            </NScrollbar>
          </aside>

          <main id="main-content" class="document-main" tabindex="-1">
            <template v-if="page.isNotFound">
              <div class="not-found">
                <p class="eyebrow">404 / 页面不存在</p>
                <h1>这篇文档还没有找到。</h1>
                <p>请从目录重新选择，或搜索需要的内容。</p>
                <a :href="withBase('/guide/introduction')">返回项目指南 →</a>
              </div>
            </template>
            <template v-else>
              <div class="page-category">{{ currentGroup?.text || 'Whim 文档' }}</div>
              <article class="doc-content" @click="copyCode"><Content /></article>
              <nav class="page-pagination" aria-label="相邻文档">
                <a v-if="adjacentPages.previous" :href="withBase(adjacentPages.previous.link)"
                  ><span>上一篇</span>{{ adjacentPages.previous.text }}</a
                >
                <a
                  v-if="adjacentPages.next"
                  class="next-page"
                  :href="withBase(adjacentPages.next.link)"
                  ><span>下一篇</span>{{ adjacentPages.next.text }} →</a
                >
              </nav>
              <footer class="page-footer">Whim · 以代码为准，让复用更简单。</footer>
            </template>
          </main>

          <aside
            v-if="!page.isNotFound && page.headers.length"
            class="outline"
            aria-label="当前页面目录"
          >
            <p>本页目录</p>
            <NAnchor :offset-top="96" :show-background="false" :show-rail="true">
              <NAnchorLink
                v-for="header in page.headers"
                :key="header.slug"
                :title="header.title"
                :href="`#${header.slug}`"
              >
                <NAnchorLink
                  v-for="child in header.children"
                  :key="child.slug"
                  :title="child.title"
                  :href="`#${child.slug}`"
                />
              </NAnchorLink>
            </NAnchor>
          </aside>
        </div>

        <NDrawer v-model:show="mobileMenuOpen" placement="left" :width="288">
          <NDrawerContent title="Whim 文档目录" closable>
            <NMenu
              :value="activePath"
              :options="menuOptions"
              @update:value="mobileMenuOpen = false"
            />
          </NDrawerContent>
        </NDrawer>
        <SearchDialog v-if="searchOpen" v-model="searchOpen" />
      </NModalProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>
