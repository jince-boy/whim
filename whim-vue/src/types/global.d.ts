export {}

declare global {
  interface Window {
    $message: import('naive-ui').MessageApi
    $notification: import('naive-ui').NotificationApi
    $dialog: import('naive-ui').DialogApi
    $loadingBar: import('naive-ui').LoadingBarApi
    $modal: import('naive-ui').ModalApi
  }
}
