export interface OperLog {
  id: string
  title: string
  logType: number
  methodName: string
  requestMethod: string
  operName: string
  operIp: string
  operLocation:string
  requestUrl: string
  requestParam: string
  responseParam: string
  status: number
  errorMessage: string
  operTime: string
  costTime: number
}

export interface OperLogResult {
  id: string
  title: string
  logType: number
  methodName: string
  requestMethod: string
  operName: string
  operIp: string
  requestUrl: string
  requestParam: string
  responseParam: string
  status: number
  errorMessage: string
  operTime: string
  costTime: number
}

export interface OperLogPageResult {
  currentPage: number
  data: OperLogResult[]
  pages: number
  size: number
  total: number
}
