<script setup lang="ts">
import { NCard, NEl, NGi, NGrid, NTag, NText } from 'naive-ui'
import AppIcon from '@/components/appIcon/AppIcon.vue'
import { pageCardThemeOverrides } from '@/config/NaiveTheme'
import systemSetting from '@/config/SystemSetting'
import { capabilities, technologyGroups } from './homeContent'

defineOptions({ name: 'HomePage' })
</script>

<template>
  <div class="framework-home">
    <n-el>
      <n-card :theme-overrides="pageCardThemeOverrides">
        <article class="overview">
          <header class="introduction">
            <div>
              <h1>{{ systemSetting.title }} · 通用后台框架</h1>
              <p><n-text depth="2">{{ systemSetting.description }}。基于 Vue 和 Spring Boot，集成组织权限、文件服务和运行监控。</n-text></p>
            </div>
            <n-tag size="small" round :bordered="false">后端 v0.0.1</n-tag>
          </header>

          <section aria-label="技术栈">
            <div v-for="group in technologyGroups" :key="group.label" class="technology-band">
              <div class="band-label" :class="group.label.toLowerCase()">
                <span lang="en">{{ group.label }}</span>
                <h2>{{ group.title }}</h2>
              </div>
              <dl class="technologies">
                <div v-for="[technology, purpose] in group.items" :key="technology">
                  <dt>{{ technology }}</dt>
                  <dd><n-text depth="3">{{ purpose }}</n-text></dd>
                </div>
              </dl>
            </div>
          </section>

          <section class="capabilities">
            <h2>框架内置能力</h2>
            <n-grid cols="1 440:2 820:3" :x-gap="10" :y-gap="10">
              <n-gi v-for="item in capabilities" :key="item.title">
                <div class="capability">
                  <AppIcon :name="item.icon" :size="18" />
                  <div>
                    <h3>{{ item.title }}</h3>
                    <p><n-text depth="2">{{ item.description }}</n-text></p>
                  </div>
                </div>
              </n-gi>
            </n-grid>
          </section>
        </article>
      </n-card>
    </n-el>
  </div>
</template>

<style scoped>
.framework-home {
  min-width: 0;
  container-type: inline-size;
}

.overview {
  padding: clamp(2px, 1.4cqw, 18px);
  overflow-wrap: anywhere;
}

.overview h1, .overview h2, .overview h3, .overview p, .technologies, .technologies dd {
  margin: 0;
}

.introduction {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px 18px;
  margin-bottom: 18px;
}

.introduction h1 {
  font-size: clamp(22px, 2.6cqw, 32px);
  line-height: 1.28;
  letter-spacing: -0.035em;
}

.introduction p {
  margin-top: 6px;
}

.technology-band {
  display: grid;
  grid-template-columns: clamp(112px, 16cqw, 172px) minmax(0, 1fr);
  gap: clamp(12px, 2cqw, 28px);
  border-top: 1px solid var(--divider-color);
  padding: 13px 0;
}

.band-label {
  --band-color: var(--success-color);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px 14px;
  border-radius: 7px;
  background-color: color-mix(in srgb, var(--band-color) 12%, var(--card-color));
}

.band-label.backend {
  --band-color: var(--info-color);
}

.band-label.infra {
  --band-color: color-mix(in srgb, var(--info-color) 60%, var(--error-color));
}

.band-label span {
  font: 600 11px/1.5 var(--app-font-family-mono);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--band-color) 70%, var(--text-color-1));
}

.overview h2 {
  font-size: 18px;
  line-height: 1.5;
}

.technologies {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-content: center;
  gap: 12px 16px;
}

.technologies dt, .capability h3 {
  font-size: 14px;
  font-weight: 600;
}

.technologies dd {
  font-size: 12px;
}

.capabilities {
  border-top: 1px solid var(--divider-color);
  padding-top: 18px;
}

.capabilities h2 {
  margin-bottom: 12px;
}

.capability {
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  height: 100%;
  gap: 10px;
  padding: 14px;
  border-radius: 7px;
  background: var(--action-color);
}

.capability > .whim-icon {
  color: var(--primary-color);
  margin-top: 2px;
}

.capability p {
  margin-top: 4px;
  font-size: 13px;
}

@container (max-width: 760px) {
  .technologies { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@container (max-width: 540px) {
  .technology-band { grid-template-columns: 80px minmax(0, 1fr); gap: 10px; }
  .band-label { padding: 8px; }
  .band-label h2 { font-size: 15px; }
  .band-label span { font-size: 10px; letter-spacing: 0.02em; }
  .technologies { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
}
</style>
