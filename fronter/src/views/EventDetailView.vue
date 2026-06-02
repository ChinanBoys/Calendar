<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Microphone } from '@element-plus/icons-vue'
import { getDemoEventById } from '@/mock/demoEvents'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()

const REMINDER_OPTIONS = [
  { label: '不提醒', value: 0 },
  { label: '提前5分钟', value: 5 },
  { label: '提前15分钟', value: 15 },
  { label: '提前30分钟', value: 30 },
  { label: '提前60分钟', value: 60 },
]

const RECURRENCE_OPTIONS = [
  { label: '不重复', value: 'none' },
  { label: '每天', value: 'daily' },
  { label: '每周', value: 'weekly' },
  { label: '每月', value: 'monthly' },
]

const form = ref({
  title: '',
  allDay: false,
  startTime: '',
  endTime: '',
  location: '',
  reminderMinutes: 15,
  recurrence: 'none',
  note: '',
})

function loadDemoEvent() {
  const event = getDemoEventById(route.params.id)
  form.value = {
    title: event.title,
    allDay: event.allDay ?? false,
    startTime: formatDateTime(event.startTime),
    endTime: formatDateTime(event.endTime),
    location: event.location ?? '',
    reminderMinutes: 15,
    recurrence: event.recurrence ?? 'none',
    note: event.note ?? '',
  }
}

watch(() => route.params.id, loadDemoEvent, { immediate: true })

function goBack() {
  router.back()
}
</script>

<template>
  <div class="phone-frame">
    <!-- 顶栏 -->
    <header class="top-bar">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <span class="top-bar__title">事件详情</span>
      <button class="voice-edit-btn" aria-label="语音修改">
        <el-icon :size="14"><Microphone /></el-icon>
        改
      </button>
    </header>

    <!-- 表单 -->
    <div class="form-body">
      <div class="form-item">
        <label class="form-label">标题</label>
        <el-input v-model="form.title" class="form-input" placeholder="请输入标题" />
      </div>

      <div class="form-item form-item--row">
        <label class="form-label">全天</label>
        <el-switch v-model="form.allDay" />
      </div>

      <div class="form-item form-item--time">
        <div class="time-field">
          <label class="form-label">开始</label>
          <el-input v-model="form.startTime" class="form-input" readonly />
        </div>
        <div class="time-field">
          <label class="form-label">结束</label>
          <el-input v-model="form.endTime" class="form-input" readonly />
        </div>
      </div>

      <div class="form-item">
        <label class="form-label">地点</label>
        <el-input v-model="form.location" class="form-input" placeholder="请输入地点" />
      </div>

      <div class="form-item">
        <label class="form-label">提醒</label>
        <el-select v-model="form.reminderMinutes" class="form-select">
          <el-option
            v-for="opt in REMINDER_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>

      <div class="form-item">
        <label class="form-label">重复</label>
        <el-select v-model="form.recurrence" class="form-select">
          <el-option
            v-for="opt in RECURRENCE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>

      <div class="form-item">
        <label class="form-label">备注</label>
        <el-input
          v-model="form.note"
          type="textarea"
          :rows="3"
          class="form-input"
          placeholder="添加备注…"
        />
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="form-actions">
      <button class="save-btn">保存修改</button>
      <button class="delete-btn">删除此事件</button>
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
  background: #fff;
  border-radius: 36px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.top-bar {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  align-items: center;
  gap: 8px;
  padding: 14px 16px 12px;
  flex-shrink: 0;
  border-bottom: 1px solid #f0f0f0;
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
  border-radius: 50%;
  padding: 0;
}

.icon-btn:hover {
  background: #f5f5f5;
}

.top-bar__title {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.voice-edit-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  background: #fff;
  color: #5b6cff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
}

.voice-edit-btn:hover {
  background: #f8f9ff;
}

.form-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.form-item {
  margin-bottom: 18px;
}

.form-item--row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-item--time {
  display: flex;
  gap: 12px;
}

.time-field {
  flex: 1;
  min-width: 0;
}

.form-label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.form-item--row .form-label {
  margin-bottom: 0;
}

.form-input :deep(.el-input__wrapper),
.form-select :deep(.el-select__wrapper) {
  background: #f5f6fa;
  box-shadow: none;
  border-radius: 10px;
  padding: 8px 12px;
}

.form-input :deep(.el-textarea__inner) {
  background: #f5f6fa;
  box-shadow: none;
  border-radius: 10px;
  padding: 10px 12px;
  resize: none;
}

.form-select {
  width: 100%;
}

.form-actions {
  flex-shrink: 0;
  padding: 12px 16px 28px;
  border-top: 1px solid #f0f0f0;
}

.save-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  background: #5b6cff;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 10px;
  transition: opacity 0.2s;
}

.save-btn:hover {
  opacity: 0.9;
}

.delete-btn {
  width: 100%;
  height: 44px;
  border: 1px dashed #e5484d;
  border-radius: 22px;
  background: #fff5f5;
  color: #e5484d;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.delete-btn:hover {
  background: #ffecec;
}
</style>
