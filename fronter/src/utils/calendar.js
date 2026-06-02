const WEEKDAY_LABELS = ['一', '二', '三', '四', '五', '六', '日']

/** 以周一为一周起始 */
export function startOfWeek(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

export function addDays(date, n) {
  const d = new Date(date)
  d.setDate(d.getDate() + n)
  return d
}

export function addMonths(date, n) {
  const d = new Date(date)
  d.setMonth(d.getMonth() + n)
  return d
}

export function isSameDay(a, b) {
  return (
    a.getFullYear() === b.getFullYear()
    && a.getMonth() === b.getMonth()
    && a.getDate() === b.getDate()
  )
}

export function formatMonthTitle(date) {
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
}

export function formatDayAgendaTitle(date) {
  return `${date.getMonth() + 1}月${date.getDate()}日 安排`
}

export function toDateKey(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/** 生成月视图网格（含前后月补位，6 行 × 7 列） */
export function buildMonthGrid(viewDate) {
  const year = viewDate.getFullYear()
  const month = viewDate.getMonth()
  const firstDay = new Date(year, month, 1)
  const startOffset = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const gridStart = addDays(firstDay, -startOffset)

  return Array.from({ length: 42 }, (_, i) => {
    const date = addDays(gridStart, i)
    return {
      date,
      inCurrentMonth: date.getMonth() === month,
    }
  })
}

/** 生成周视图 7 天 */
export function buildWeekGrid(viewDate) {
  const start = startOfWeek(viewDate)
  return Array.from({ length: 7 }, (_, i) => {
    const date = addDays(start, i)
    return { date, inCurrentMonth: true }
  })
}

export { WEEKDAY_LABELS }
