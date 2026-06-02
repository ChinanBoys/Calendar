/** P3 日历视图演示数据（仅布局预览，暂不对接接口） */
export const DEMO_CALENDAR_EVENTS = [
  {
    id: 120,
    title: '晨会',
    startTime: '2026-06-01T09:00:00',
    endTime: '2026-06-01T10:00:00',
    location: '3号会议室',
  },
  {
    id: 123,
    title: '会议',
    startTime: '2026-06-01T15:00:00',
    endTime: '2026-06-01T17:00:00',
    location: '3号会议室',
  },
  {
    id: 125,
    title: '健身',
    startTime: '2026-06-01T19:00:00',
    endTime: '2026-06-01T20:00:00',
    location: '健身房',
  },
  {
    id: 201,
    title: '项目评审',
    startTime: '2026-06-03T14:00:00',
    endTime: '2026-06-03T16:00:00',
    location: '线上',
  },
  {
    id: 202,
    title: '团队午餐',
    startTime: '2026-06-08T12:00:00',
    endTime: '2026-06-08T13:00:00',
    location: '食堂',
  },
  {
    id: 203,
    title: '客户拜访',
    startTime: '2026-06-15T10:00:00',
    endTime: '2026-06-15T11:30:00',
    location: 'A座前台',
  },
  {
    id: 204,
    title: '月度总结',
    startTime: '2026-06-22T16:00:00',
    endTime: '2026-06-22T18:00:00',
    location: '大会议室',
  },
]

export function groupEventsByDate(events) {
  const map = {}
  for (const e of events) {
    const key = e.startTime.slice(0, 10)
    if (!map[key]) map[key] = []
    map[key].push(e)
  }
  for (const key of Object.keys(map)) {
    map[key].sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
  }
  return map
}

export const DEMO_EVENT_MAP = groupEventsByDate(DEMO_CALENDAR_EVENTS)
