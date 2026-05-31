import request from './request'

/** F-REMIND-01 — 查询即将到来的提醒 */
export function fetchUpcomingReminders(hours = 24) {
  return request.get('/reminders/upcoming', { params: { hours } })
}
