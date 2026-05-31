/** 检测事件列表中是否存在时间重叠（F-CONFLICT-01 时间轴展示） */
export function findConflictingEventIds(events) {
  const conflictIds = new Set()
  const sorted = [...events].sort(
    (a, b) => new Date(a.startTime) - new Date(b.startTime),
  )

  for (let i = 0; i < sorted.length; i++) {
    for (let j = i + 1; j < sorted.length; j++) {
      const a = sorted[i]
      const b = sorted[j]
      const aStart = new Date(a.startTime).getTime()
      const aEnd = new Date(a.endTime).getTime()
      const bStart = new Date(b.startTime).getTime()
      const bEnd = new Date(b.endTime).getTime()

      if (aStart < bEnd && bStart < aEnd) {
        conflictIds.add(a.id)
        conflictIds.add(b.id)
      }
    }
  }

  return conflictIds
}
