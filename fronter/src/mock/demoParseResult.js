/** 开发环境 API 不可用时的解析演示数据 */
export const DEMO_PARSE_RESULT = {
  intent: 'create',
  title: '会议',
  startTime: '2026-06-01T15:00:00',
  endTime: '2026-06-01T17:00:00',
  allDay: false,
  location: null,
  reminderOffsets: [15],
  recurrence: 'none',
  confidence: {
    intent: 0.99,
    title: 0.9,
    startTime: 0.95,
    endTime: 0.95,
    location: 0.4,
  },
  rawText: '我明天下午三点到五点有个会议',
}
