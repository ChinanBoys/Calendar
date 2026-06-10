<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, AlarmClock } from '@element-plus/icons-vue'
import { fetchUpcomingReminders } from '@/api/reminders'
import { formatDateTime, formatTime, parseLocalDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const reminders = ref([])

function isSameDay(a, b) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  )
}

function formatRelativeDateTime(value) {
  const time = parseLocalDateTime(value)
  if (!Number.isFinite(time)) return ''

  const date = new Date(time)
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(today.getDate() + 1)

  if (isSameDay(date, today)) return `今天 ${formatTime(value)}`
  if (isSameDay(date, tomorrow)) return `明天 ${formatTime(value)}`
  return formatDateTime(value)
}

function formatReminderTitle(item) {
  return item.eventTitle || '未命名事件'
}

function formatReminderSubtitle(item) {
  const parts = []

  const fireTime = formatRelativeDateTime(item.fireTime)
  if (fireTime) parts.push(`${fireTime}提醒`)

  const startTime = formatRelativeDateTime(item.startTime)
  if (startTime) parts.push(`事件开始 ${startTime}`)

  return parts.join(' · ')
}

function isReminderExpired(item) {
  const now = Date.now()

  const fireTime = parseLocalDateTime(item.fireTime)
  if (Number.isFinite(fireTime) && fireTime <= now) return true

  const endTime = parseLocalDateTime(item.endTime)
  return Number.isFinite(endTime) && endTime <= now
}

async function loadReminders() {
  loading.value = true
  try {
    const res = await fetchUpcomingReminders(24)
    reminders.value = (res.data ?? []).filter((item) => !isReminderExpired(item))
  } catch {
    reminders.value = []
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

function onItemClick(item) {
  if (item.eventId) {
    router.push(`/events/${item.eventId}`)
  }
}

onMounted(loadReminders)
</script>

<template>
  <div class="phone-frame">
    <!-- 顶栏 -->
    <header class="top-bar">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <span class="top-bar__title">提醒</span>
      <span class="top-bar__spacer" />
    </header>

    <!-- 内容区 -->
    <div class="content">
      <!-- 即将到来 -->
      <section v-loading="loading" class="section">
        <h2 class="section-title">即将到来</h2>
        <div class="card-list">
          <div
            v-for="item in reminders"
            :key="item.id"
            class="reminder-card"
            @click="onItemClick(item)"
          >
            <div class="reminder-card__icon">
              <el-icon :size="20" color="#e5484d"><AlarmClock /></el-icon>
            </div>
            <div class="reminder-card__body">
              <div class="reminder-card__title">{{ formatReminderTitle(item) }}</div>
              <div class="reminder-card__subtitle">{{ formatReminderSubtitle(item) }}</div>
            </div>
          </div>
          <div v-if="!loading && reminders.length === 0" class="empty-state">
            暂无即将到来的提醒
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.phone-frame {
  display: flex;
  flex-direction: column;
  width: 375px;
  height: 812px;
  margin: 0 auto;
  background: #f5f7fa;
  border-radius: 36px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.top-bar {
  display: grid;
  grid-template-columns: 36px 1fr 36px;
  align-items: center;
  padding: 14px 16px 12px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  color: #333;
  cursor: pointer;
  border-radius: 50%;
  padding: 0;
}

.icon-btn:hover {
  background: #eee;
}

.top-bar__title {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.top-bar__spacer {
  width: 36px;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.section {
  min-height: 120px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #999;
  margin-bottom: 10px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reminder-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background 0.2s;
}

.reminder-card:hover {
  background: #fafbff;
}

.reminder-card__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-top: 2px;
}

.reminder-card__body {
  flex: 1;
  min-width: 0;
}

.reminder-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
  margin-bottom: 4px;
}

.reminder-card__subtitle {
  font-size: 13px;
  color: #999;
  line-height: 1.3;
}

.empty-state {
  padding: 32px 16px;
  text-align: center;
  font-size: 14px;
  color: #aaa;
  background: #fff;
  border-radius: 12px;
}
</style>
