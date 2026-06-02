<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight, Microphone } from '@element-plus/icons-vue'
import AgendaEventItem from '@/components/calendar/AgendaEventItem.vue'
import {
  WEEKDAY_LABELS,
  buildMonthGrid,
  buildWeekGrid,
  addDays,
  addMonths,
  isSameDay,
  formatMonthTitle,
  formatDayAgendaTitle,
  toDateKey,
} from '@/utils/calendar'
import { DEMO_EVENT_MAP } from '@/mock/demoCalendarEvents'

const route = useRoute()
const router = useRouter()

const viewMode = ref(route.query.view === 'month' ? 'month' : 'week')
const viewDate = ref(new Date(2026, 5, 1))
const selectedDate = ref(new Date(2026, 5, 1))

watch(
  () => route.query.view,
  (v) => {
    viewMode.value = v === 'month' ? 'month' : 'week'
  },
)

const monthTitle = computed(() => formatMonthTitle(viewDate.value))

const calendarCells = computed(() =>
  viewMode.value === 'month'
    ? buildMonthGrid(viewDate.value)
    : buildWeekGrid(viewDate.value),
)

const agendaTitle = computed(() => formatDayAgendaTitle(selectedDate.value))

const dayEvents = computed(() => {
  const key = toDateKey(selectedDate.value)
  return DEMO_EVENT_MAP[key] ?? []
})

const datesWithEvents = computed(() => new Set(Object.keys(DEMO_EVENT_MAP)))

function hasEvent(date) {
  return datesWithEvents.value.has(toDateKey(date))
}

function isSelected(date) {
  return isSameDay(date, selectedDate.value)
}

function onSelectDate(date) {
  selectedDate.value = new Date(date)
}

function onPrev() {
  if (viewMode.value === 'month') {
    viewDate.value = addMonths(viewDate.value, -1)
  } else {
    viewDate.value = addDays(viewDate.value, -7)
    selectedDate.value = addDays(selectedDate.value, -7)
  }
}

function onNext() {
  if (viewMode.value === 'month') {
    viewDate.value = addMonths(viewDate.value, 1)
  } else {
    viewDate.value = addDays(viewDate.value, 7)
    selectedDate.value = addDays(selectedDate.value, 7)
  }
}

function switchView(mode) {
  viewMode.value = mode
  router.replace({ path: '/calendar', query: { view: mode } })
}

function goBack() {
  router.push('/')
}

function onEventClick(event) {
  router.push(`/events/${event.id}`)
}
</script>

<template>
  <div class="phone-frame">
    <!-- 顶栏 -->
    <header class="top-bar">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <span class="top-bar__title">{{ monthTitle }}</span>
      <div class="view-toggle">
        <button
          class="toggle-btn"
          :class="{ active: viewMode === 'week' }"
          @click="switchView('week')"
        >
          周
        </button>
        <button
          class="toggle-btn"
          :class="{ active: viewMode === 'month' }"
          @click="switchView('month')"
        >
          月
        </button>
      </div>
    </header>

    <!-- 日历区 -->
    <section class="calendar-section">
      <div class="month-nav">
        <button class="nav-btn" aria-label="上一页" @click="onPrev">
          <el-icon :size="16"><ArrowLeft /></el-icon>
        </button>
        <span class="month-nav__title">{{ monthTitle }}</span>
        <button class="nav-btn" aria-label="下一页" @click="onNext">
          <el-icon :size="16"><ArrowRight /></el-icon>
        </button>
      </div>

      <div class="weekday-row">
        <span v-for="label in WEEKDAY_LABELS" :key="label" class="weekday-cell">
          {{ label }}
        </span>
      </div>

      <div class="date-grid" :class="{ 'date-grid--week': viewMode === 'week' }">
        <button
          v-for="(cell, idx) in calendarCells"
          :key="idx"
          class="date-cell"
          :class="{
            'date-cell--outside': !cell.inCurrentMonth,
            'date-cell--selected': isSelected(cell.date),
          }"
          @click="onSelectDate(cell.date)"
        >
          <span class="date-num">{{ cell.date.getDate() }}</span>
          <span v-if="hasEvent(cell.date)" class="event-dot" />
        </button>
      </div>
    </section>

    <!-- 当日安排 -->
    <section class="agenda-section">
      <h2 class="agenda-title">{{ agendaTitle }}</h2>
      <div v-if="dayEvents.length" class="agenda-list">
        <AgendaEventItem
          v-for="event in dayEvents"
          :key="event.id"
          :event="event"
          @click="onEventClick"
        />
      </div>
      <div v-else class="agenda-empty">这一天还没有安排</div>
    </section>

    <!-- 悬浮语音按钮 -->
    <button class="fab-voice" aria-label="语音输入">
      <el-icon :size="28"><Microphone /></el-icon>
    </button>
  </div>
</template>

<style scoped>
.phone-frame {
  display: flex;
  flex-direction: column;
  width: 375px;
  height: 812px;
  margin: 0 auto;
  background: #fff;
  border-radius: 36px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  position: relative;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px 10px;
  flex-shrink: 0;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #333;
  cursor: pointer;
  border-radius: 8px;
  padding: 0;
}

.icon-btn:hover {
  background: #f5f5f5;
}

.top-bar__title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.view-toggle {
  display: flex;
  border: 1px solid #5b6cff;
  border-radius: 16px;
  overflow: hidden;
}

.toggle-btn {
  width: 40px;
  height: 30px;
  border: none;
  background: #fff;
  color: #5b6cff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn.active {
  background: #5b6cff;
  color: #fff;
}

.calendar-section {
  padding: 0 12px 12px;
  flex-shrink: 0;
  background: #f8f9ff;
  border-bottom: 1px solid #eef0f5;
}

.month-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 12px;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  border-radius: 50%;
}

.nav-btn:hover {
  background: rgba(91, 108, 255, 0.08);
}

.month-nav__title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.weekday-cell {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 4px 0;
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px 0;
}

.date-grid--week {
  grid-template-rows: 1fr;
}

.date-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  height: 44px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  position: relative;
}

.date-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #333;
  border-radius: 50%;
  transition: all 0.15s;
}

.date-cell--outside .date-num {
  color: #ccc;
}

.date-cell--selected .date-num {
  background: #5b6cff;
  color: #fff;
  font-weight: 600;
}

.date-cell:not(.date-cell--selected):hover .date-num {
  background: #eef0ff;
}

.event-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #ff7a59;
  position: absolute;
  bottom: 4px;
}

.date-cell--selected .event-dot {
  background: #fff;
}

.agenda-section {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.agenda-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.agenda-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.agenda-empty {
  text-align: center;
  color: #aaa;
  font-size: 14px;
  padding: 32px 16px;
}

.fab-voice {
  position: absolute;
  right: 20px;
  bottom: 28px;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: #5b6cff;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(91, 108, 255, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: transform 0.15s;
}

.fab-voice:hover {
  transform: scale(1.05);
}
</style>
