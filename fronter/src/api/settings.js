import request from './request'

/** F-SET-01 — 查询设置 */
export function fetchSettings() {
  return request.get('/settings')
}

/** F-SET-01 — 修改设置 */
export function updateSettings(data) {
  return request.put('/settings', data)
}

/** F-SET-02 — 清除语音数据 */
export function clearVoiceLogs() {
  return request.delete('/voice-logs')
}
