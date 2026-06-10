import { parseLocalDateTime } from './date'

export const REMINDERS_CHANGED_EVENT = 'voicecal:reminders-changed'

export function isReminderExpired(item, now = Date.now()) {
  const endTime = parseLocalDateTime(item?.endTime)
  const reference = now instanceof Date ? now.getTime() : Number(now)

  return !Number.isFinite(endTime) || !Number.isFinite(reference) || endTime <= reference
}

export function filterActiveReminders(reminders, now = Date.now()) {
  return (reminders ?? []).filter((item) => !isReminderExpired(item, now))
}

export function countUnreadReminders(reminders, now = Date.now()) {
  return filterActiveReminders(reminders, now).filter((item) => !item.sent).length
}

export function notifyRemindersChanged(detail = {}) {
  window.dispatchEvent(new CustomEvent(REMINDERS_CHANGED_EVENT, { detail }))
}
