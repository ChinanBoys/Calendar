<script setup>
import { Location } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/date'

defineProps({
  event: { type: Object, required: true },
})

defineEmits(['click'])
</script>

<template>
  <div class="agenda-item" @click="$emit('click', event)">
    <div class="agenda-item__time">
      <span class="time-start">{{ formatTime(event.startTime) }}</span>
      <span class="time-end">{{ formatTime(event.endTime) }}</span>
    </div>
    <div class="agenda-item__divider" />
    <div class="agenda-item__body">
      <div class="agenda-item__title">{{ event.title }}</div>
      <div v-if="event.location" class="agenda-item__location">
        <el-icon :size="13"><Location /></el-icon>
        <span>{{ event.location }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.agenda-item {
  display: flex;
  align-items: stretch;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: background 0.2s;
}

.agenda-item:hover {
  background: #fafbff;
}

.agenda-item__time {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  gap: 2px;
}

.time-start {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.time-end {
  font-size: 12px;
  color: #999;
}

.agenda-item__divider {
  width: 3px;
  border-radius: 2px;
  background: #5b6cff;
  flex-shrink: 0;
}

.agenda-item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.agenda-item__title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}

.agenda-item__location {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 13px;
  color: #888;
}
</style>
