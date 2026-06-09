<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Search, Bell } from '@element-plus/icons-vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import EventCard from '@/components/today/EventCard.vue'
import VoicePanel from '@/components/today/VoicePanel.vue'
import ConfirmCard from '@/components/today/ConfirmCard.vue'
import { fetchEvents, deleteEvent, restoreEvent } from '@/api/events'
import { fetchUpcomingReminders } from '@/api/reminders'
import { formatTodayHeader, getDayRange, filterEventsForDay, isPastEvent } from '@/utils/date'
import { findConflictingEventIds } from '@/utils/conflict'

const router = useRouter()

const activeTab = ref('today')
const events = ref([])
const loading = ref(false)
const reminderCount = ref(0)
const now = ref(new Date())
let clockTimer = null
const todayHeader = computed(() => formatTodayHeader(new Date()))

const conflictIds = computed(() => findConflictingEventIds(events.value))

const showConfirm = ref(false)
const parseResult = ref(null)
const voicePanelRef = ref(null)

/** F-VIEW-01 — 加载今日事件（严格按当天日期过滤，仅展示后端真实数据） */
async function loadTodayEvents() {
  loading.value = true
  const today = new Date()
  try {
    const { from, to } = getDayRange(today)
    const res = await fetchEvents({ from, to, page: 1, pageSize: 50 })
    const rows = res.data?.rows ?? []
    events.value = filterEventsForDay(rows, today)
  } catch {
    events.value = []
  } finally {
    loading.value = false
  }
}

/** F-REMIND-01 — 提醒数量角标 */
async function loadReminderBadge() {
  try {
    const res = await fetchUpcomingReminders(24)
    reminderCount.value = res.data?.length ?? 0
  } catch {
    reminderCount.value = 0
  }
}

/** F-SET — 设置入口 */
function goSettings() {
  router.push('/settings')
}

/** F-VIEW-05 — 搜索 */
function goSearch() {
  router.push('/search')
}

/** F-REMIND-01 — 提醒中心 */
function goReminders() {
  router.push('/reminders')
}

/** F-VIEW-02 — 周/月视图切换 */
function onTabChange(tab) {
  if (tab === 'today') {
    activeTab.value = 'today'
    loadTodayEvents()
    return
  }
  router.push({ path: '/calendar', query: { view: tab } })
}

/** F-VIEW-04 — 事件详情 */
function onEventClick(event) {
  if (isEventPast(event)) return
  router.push(`/events/${event.id}`)
}

function isEventPast(event) {
  return isPastEvent(event, now.value)
}

/** F-DEL-02 — 左滑删除 + 撤销 */
async function onEventDelete(event) {
  try {
    await ElMessageBox.confirm(`确定删除「${event.title}」？`, '删除事件', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteEvent(event.id)
    events.value = events.value.filter((e) => e.id !== event.id)

    ElNotification({
      title: '已删除',
      message: '点击通知可撤销',
      type: 'success',
      duration: 5000,
      onClick: async () => {
        try {
          await restoreEvent(event.id)
          loadTodayEvents()
        } catch {
          /* handled */
        }
      },
    })
  } catch {
    /* 用户取消 */
  }
}

function onVoiceParsed() {
  loadTodayEvents()
}

/** 语音解析完成 → 打开 P2 确认卡 */
function onVoiceConfirm(data) {
  parseResult.value = data
  showConfirm.value = true
}

function onConfirmSaved() {
  showConfirm.value = false
  parseResult.value = null
  loadTodayEvents()
}

function onConfirmRetalk() {
  showConfirm.value = false
  voicePanelRef.value?.promptVoiceInput(parseResult.value?.rawText ?? '')
}

onMounted(() => {
  loadTodayEvents()
  loadReminderBadge()
  clockTimer = window.setInterval(() => {
    now.value = new Date()
  }, 60 * 1000)
})

onUnmounted(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
    clockTimer = null
  }
})
</script>

<template>
  <div class="phone-frame">
    <!-- 顶栏 -->
    <header class="top-bar">
      <button class="icon-btn" aria-label="设置" @click="goSettings">
        <el-icon :size="22"><Fold /></el-icon>
      </button>
      <div class="top-bar__title">
        <div class="top-bar__date">{{ todayHeader }}</div>
        <div class="top-bar__subtitle">VoiceCal 语音日历</div>
      </div>
      <div class="top-bar__actions">
        <button class="icon-btn" aria-label="搜索" @click="goSearch">
          <el-icon :size="20"><Search /></el-icon>
        </button>
        <button class="icon-btn badge-wrap" aria-label="提醒" @click="goReminders">
          <el-icon :size="20"><Bell /></el-icon>
          <span v-if="reminderCount > 0" class="badge">{{ reminderCount }}</span>
        </button>
      </div>
    </header>

    <!-- 视图 Tab -->
    <div class="view-tabs">
      <button
        class="view-tab"
        :class="{ active: activeTab === 'today' }"
        @click="onTabChange('today')"
      >
        今日
      </button>
      <button class="view-tab" @click="onTabChange('week')">周</button>
      <button class="view-tab" @click="onTabChange('month')">月</button>
    </div>

    <!-- 今日时间轴 -->
    <section class="timeline-section">
      <h2 class="section-title">今日时间轴</h2>

      <div v-loading="loading" class="timeline-list">
        <template v-if="events.length">
          <EventCard
            v-for="event in events"
            :key="event.id"
            :event="event"
            :has-conflict="!isEventPast(event) && conflictIds.has(event.id)"
            :is-past="isEventPast(event)"
            @click="onEventClick(event)"
            @delete="onEventDelete(event)"
          />
        </template>
        <div v-else-if="!loading" class="empty-state">
          今天还没有安排，说一句试试
        </div>
      </div>
    </section>

    <!-- 语音交互区 -->
    <VoicePanel
      ref="voicePanelRef"
      @parsed="onVoiceParsed"
      @confirm="onVoiceConfirm"
    />

    <!-- P2 解析确认卡 -->
    <ConfirmCard
      v-model:visible="showConfirm"
      :parse-result="parseResult"
      @saved="onConfirmSaved"
      @retalk="onConfirmRetalk"
    />
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
  min-width: 0;
}

.top-bar__date {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.3;
}

.top-bar__subtitle {
  font-size: 12px;
  color: #999;
  line-height: 1.3;
}

.top-bar__actions {
  display: flex;
  gap: 2px;
}

.badge-wrap {
  position: relative;
}

.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  color: #fff;
  background: #e5484d;
  border-radius: 8px;
}

.view-tabs {
  display: flex;
  gap: 8px;
  padding: 0 16px 12px;
  flex-shrink: 0;
}

.view-tab {
  flex: 1;
  height: 34px;
  border: 1px solid #5b6cff;
  border-radius: 17px;
  background: #fff;
  color: #5b6cff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.view-tab.active {
  background: #5b6cff;
  color: #fff;
}

.view-tab:not(.active):hover {
  background: #eef0ff;
}

.timeline-section {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
  min-height: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.timeline-list {
  min-height: 120px;
}

.empty-state {
  text-align: center;
  color: #aaa;
  font-size: 14px;
  padding: 40px 16px;
}
</style>
