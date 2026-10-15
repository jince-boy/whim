<script setup lang="ts">
import { NButton, NCard, NTag } from 'naive-ui'
import { withBase } from 'vitepress'

const entries = [
  {
    number: '01',
    label: '组件手册',
    title: '把常用交互，做好一次。',
    text: '表格、查询表单、弹窗与图标。从最小示例到参数与边界，找到正确的接入方式。',
    link: '/components/overview',
    action: '浏览自定义组件',
    tags: ['注册式 Hook', '类型推导', '真实源码演示'],
  },
  {
    number: '02',
    label: '权限与认证',
    title: '每一次访问，都有清晰边界。',
    text: '理解功能权限、数据范围和认证会话如何协作，再把规则准确地接入业务。',
    link: '/backend/data-permission',
    action: '阅读权限指南',
    tags: ['数据权限', 'Sa-Token', '多账号接入'],
  },
  {
    number: '03',
    label: '工程指南',
    title: '沿着项目约定，继续构建。',
    text: '从前后端模块边界出发，统一页面组织、公共能力与服务层的开发方式。',
    link: '/guide/introduction',
    action: '了解 Whim',
    tags: ['Vue 3', 'Naive UI', 'Spring Boot'],
  },
]
</script>

<template>
  <main class="home-page">
    <section class="home-hero">
      <div class="hero-copy">
        <div class="eyebrow"><span class="status-dot" /> WHIM / DEVELOPER HANDBOOK</div>
        <h1>从组件到权限，<br /><span>让开发有据可循。</span></h1>
        <p>
          把项目里值得复用的能力，整理成随手可查的开发手册。<br
            class="desktop-break"
          />示例来自现有实现，规则落到真实的代码边界。
        </p>
        <div class="hero-actions">
          <NButton tag="a" :href="withBase('/guide/getting-started')" type="primary" size="large">
            开始阅读 <span class="button-arrow">↗</span>
          </NButton>
          <a class="hero-secondary" :href="withBase('/components/overview')">查看组件手册 →</a>
        </div>
        <div class="hero-footnote">一个项目 · 前后端共用的知识入口</div>
      </div>
      <div class="architecture-card" aria-label="Whim 能力结构图">
        <div class="architecture-topline">
          <span class="status-dot" /> WHIM SYSTEM <span>框架能力</span>
        </div>
        <div class="architecture-heading">有一致体验，也有明确边界。</div>
        <div class="architecture-row">
          <span>交互层</span>
          <div><b>WhimTable</b><b>SearchForm</b><b>Dialog</b></div>
        </div>
        <div class="architecture-connector">↓ <span>统一接入约定</span></div>
        <div class="architecture-row">
          <span>业务层</span>
          <div><b>Service</b><b>AuthenticationContext</b></div>
        </div>
        <div class="architecture-connector">↓ <span>按职责组合能力</span></div>
        <div class="architecture-row">
          <span>基础层</span>
          <div><b>DataPermission</b><b>Sa-Token</b></div>
        </div>
        <div class="architecture-bottom">SOURCE FIRST <span>文档与实现相互对应</span></div>
      </div>
    </section>
    <section class="home-entries" aria-label="文档分类">
      <a
        v-for="entry in entries"
        :key="entry.number"
        :href="withBase(entry.link)"
        class="entry-link"
      >
        <NCard :bordered="false" class="entry-card">
          <div class="entry-label">
            <span>{{ entry.label }}</span
            ><span>{{ entry.number }}</span>
          </div>
          <h2>{{ entry.title }}</h2>
          <p>{{ entry.text }}</p>
          <div class="entry-tags">
            <NTag v-for="tag in entry.tags" :key="tag" size="small" :bordered="false">{{
              tag
            }}</NTag>
          </div>
          <div class="entry-action">{{ entry.action }} <span>↗</span></div>
        </NCard>
      </a>
    </section>
    <footer class="home-footer">
      <span class="site-brand"
        >Whim <span class="brand-caption">/ 写好代码，也写清楚用法。</span></span
      ><span>Vue 3 · Naive UI · Spring Boot · Sa-Token</span>
    </footer>
  </main>
</template>
