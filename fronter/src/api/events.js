import request from './request'

/** F-VIEW-01 / F-VIEW-02 / F-VIEW-03 / F-VIEW-05 — 事件列表查询 */
export function fetchEvents(params) {
  return request.get('/events', { params })
}

/** F-VIEW-04 — 事件详情 */
export function fetchEventById(id) {
  return request.get(`/events/${id}`)
}

/** F-ADD-01/02/03 — 创建事件 */
export function createEvent(data) {
  return request.post('/events', data)
}

/** F-EDIT-01/02 — 修改事件 */
export function updateEvent(id, data) {
  return request.put(`/events/${id}`, data)
}

/** F-DEL-01/02 — 删除事件 */
export function deleteEvent(id) {
  return request.delete(`/events/${id}`)
}

/** F-DEL-02 — 撤销删除 */
export function restoreEvent(id) {
  return request.put(`/events/${id}/restore`)
}

/** F-CONFLICT-01 — 冲突检测 */
export function checkConflicts(params) {
  return request.get('/events/conflicts', { params })
}
