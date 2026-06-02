/** 后端未就绪时的演示数据，便于预览 P1 布局 */
export const DEMO_EVENTS = [
  {
    id: 120,
    userId: 1,
    title: '晨会',
    startTime: '2026-06-01T09:00:00',
    endTime: '2026-06-01T10:00:00',
    allDay: false,
    location: '3号会议室',
    note: null,
    recurrence: 'none',
    status: 1,
  },
  {
    id: 77,
    userId: 1,
    title: '周会',
    startTime: '2026-06-01T14:30:00',
    endTime: '2026-06-01T15:30:00',
    allDay: false,
    location: '2号会议室',
    note: null,
    recurrence: 'none',
    status: 1,
  },
  {
    id: 123,
    userId: 1,
    title: '会议',
    startTime: '2026-06-01T15:00:00',
    endTime: '2026-06-01T17:00:00',
    allDay: false,
    location: '3号会议室',
    note: null,
    recurrence: 'none',
    status: 1,
  },
  {
    id: 125,
    userId: 1,
    title: '健身',
    startTime: '2026-06-01T19:00:00',
    endTime: '2026-06-01T20:00:00',
    allDay: false,
    location: '健身房',
    note: null,
    recurrence: 'none',
    status: 1,
  },
]

export function getDemoEventById(id) {
  return DEMO_EVENTS.find((e) => e.id === Number(id)) ?? DEMO_EVENTS[2]
}
