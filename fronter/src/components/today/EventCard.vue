<script setup>
import { ref } from 'vue'
import { Location, WarningFilled } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'

const props = defineProps({
  event: { type: Object, required: true },
  hasConflict: { type: Boolean, default: false },
  isPast: { type: Boolean, default: false },
})

const emit = defineEmits(['click', 'delete'])

const startX = ref(0)
const offsetX = ref(0)
const swiping = ref(false)

function onTouchStart(e) {
  startX.value = e.touches[0].clientX
  swiping.value = true
}

function onTouchMove(e) {
  if (!swiping.value) return
  const dx = e.touches[0].clientX - startX.value
  offsetX.value = Math.min(0, Math.max(dx, -80))
}

function onTouchEnd() {
  if (offsetX.value < -50) {
    emit('delete')
  }
  offsetX.value = 0
  swiping.value = false
}

function onCardClick() {
  if (props.isPast) return
  emit('click')
}
</script>

<template>
  <div
    class="event-card-wrap"
    @touchstart.passive="onTouchStart"
    @touchmove.passive="onTouchMove"
    @touchend="onTouchEnd"
  >
    <div class="delete-action">删除</div>
    <div
      class="event-card"
      :class="{ conflict: hasConflict, 'event-card--past': isPast }"
      :style="{ transform: `translateX(${offsetX}px)` }"
      @click="onCardClick"
    >
      <div class="event-card__time">
        <span class="time-start">{{ formatTime(event.startTime) }}</span>
        <span class="time-end">{{ formatTime(event.endTime) }}</span>
      </div>
      <div class="event-card__body">
        <div class="event-card__title-row">
          <span class="event-card__title">{{ event.title }}</span>
          <span v-if="hasConflict" class="conflict-badge">
            <el-icon :size="12"><WarningFilled /></el-icon>
            时间冲突
          </span>
        </div>
        <div v-if="event.location" class="event-card__location">
          <el-icon :size="13"><Location /></el-icon>
          <span>{{ event.location }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.event-card-wrap {
  position: relative;
  margin-bottom: 10px;
  overflow: hidden;
  border-radius: 12px;
}

.delete-action {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e5484d;
  color: #fff;
  font-size: 14px;
  border-radius: 0 12px 12px 0;
}

.event-card {
  display: flex;
  gap: 12px;
  padding: 14px 14px 14px 16px;
  background: #f8f9ff;
  border-radius: 12px;
  border-left: 3px solid #5b6cff;
  cursor: pointer;
  transition: transform 0.15s ease;
  position: relative;
  z-index: 1;
}

.event-card.conflict {
  border-left-color: #e5484d;
}

.event-card--past {
  background: #f1f2f5;
  border-left-color: #c6c8cf;
  cursor: default;
}

.event-card--past.conflict {
  border-left-color: #c6c8cf;
}

.event-card__time {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 44px;
  gap: 2px;
}

.time-start {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.event-card--past .time-start,
.event-card--past .time-end,
.event-card--past .event-card__title,
.event-card--past .event-card__location {
  color: #a5a7ad;
}

.time-end {
  font-size: 12px;
  color: #999;
}

.event-card__body {
  flex: 1;
  min-width: 0;
}

.event-card__title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.event-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.conflict-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  color: #b8860b;
  background: #fff3cd;
  border-radius: 10px;
}

.event-card__location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 13px;
  color: #888;
}
</style>
