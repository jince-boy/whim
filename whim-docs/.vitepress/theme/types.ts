import type { InjectionKey } from 'vue'

export const collectStylesKey: InjectionKey<() => string> = Symbol('collect-naive-styles')

export interface SearchPage {
  title: string
  url: string
  text: string
}
