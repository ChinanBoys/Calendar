const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export function formatTime(isoString) {
  if (!isoString) return ''
  const date = new Date(isoString)
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

/** 格式：6月1日 15:00 */
export function formatDateTime(isoString) {
  if (!isoString) return ''
  const date = new Date(isoString)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日 ${formatTime(isoString)}`
}

export function formatTodayHeader(date = new Date()) {
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekday = WEEKDAYS[date.getDay()]
  return `今日 · ${month}月${day}日 ${weekday}`
}

/** YYYY-MM-DD，用于与后端 startTime 日期部分比对 */
export function getDateKey(date = new Date()) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function getEventDateKey(isoString) {
  if (!isoString) return ''
  return String(isoString).slice(0, 10)
}

/** 仅保留「开始日期 = 当天」的事件，并按开始时间升序 */
export function filterEventsForDay(events, date = new Date()) {
  const dayKey = getDateKey(date)
  return [...events]
    .filter((e) => getEventDateKey(e.startTime) === dayKey)
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
}

export function getDayRange(date = new Date()) {
  const dayKey = getDateKey(date)
  return {
    from: `${dayKey}T00:00:00`,
    to: `${dayKey}T23:59:59`,
  }
}

export function toIsoLocal(date) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export function nowIso() {
  return new Date().toISOString()
}
