export type HomeId = string | number

export interface HomePageQuery {
  targetType: number | null
  status: number | null
}

export interface HomePageSave {
  id?: HomeId
  targetType: number
  targetId: HomeId | null
  path: string
  component: string
  priority: number
  status: number
}

export interface HomePageRow extends HomePageSave {
  id: HomeId
  targetName: string | null
}

export interface HomeTarget {
  id: HomeId
  name: string
}
