function parseLocalDateTime(value) {
  if (!value) return Number.NaN

  const text = String(value).trim()
  const match = text.match(
    /^(\d{4})-(\d{2})-(\d{2})(?:T|\s)(\d{2}):(\d{2})(?::(\d{2}))?/,
  )

  if (match) {
    const [, year, month, day, hour, minute, second = '0'] = match
    return new Date(
      Number(year),
      Number(month) - 1,
      Number(day),
      Number(hour),
      Number(minute),
      Number(second),
    ).getTime()
  }

  return new Date(value).getTime()
}

function getTimedInterval(event) {
  if (!event || event.allDay) return null

  const start = parseLocalDateTime(event.startTime)
  const end = parseLocalDateTime(event.endTime)

  if (!Number.isFinite(start) || !Number.isFinite(end) || start >= end) {
    return null
  }

  return {
    id: event.id,
    start,
    end,
  }
}

/** 检测事件列表中是否存在时间重叠（F-CONFLICT-01 时间轴展示） */
export function findConflictingEventIds(events) {
  const conflictIds = new Set()
  const sorted = events
    .map(getTimedInterval)
    .filter(Boolean)
    .sort((a, b) => a.start - b.start)

  for (let i = 0; i < sorted.length; i++) {
    for (let j = i + 1; j < sorted.length; j++) {
      const a = sorted[i]
      const b = sorted[j]

      if (b.start >= a.end) break

      if (a.start < b.end && b.start < a.end) {
        conflictIds.add(a.id)
        conflictIds.add(b.id)
      }
    }
  }

  return conflictIds
}
