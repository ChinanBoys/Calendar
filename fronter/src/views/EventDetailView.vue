<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Microphone } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteEvent, fetchEventById, updateEvent } from '@/api/events'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)

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

function getReminderMinutes(event) {
  const firstReminder = Array.isArray(event.reminders) ? event.reminders[0] : null
  return firstReminder?.offsetMinutes ?? 0
}

function fillForm(event) {
  form.value = {
    title: event.title ?? '',
    allDay: event.allDay ?? false,
    startTime: event.startTime ?? '',
    endTime: event.endTime ?? '',
    location: event.location ?? '',
    reminderMinutes: getReminderMinutes(event),
    recurrence: event.recurrence ?? 'none',
    note: event.note ?? '',
  }
}

async function loadEventDetail(id) {
  if (!id) return

  loading.value = true
  try {
    const res = await fetchEventById(id)
    if (!res.data) {
      ElMessage.warning('事件不存在或已被删除')
      return
    }
    fillForm(res.data)
  } catch {
    /* 错误已由请求拦截器提示 */
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, loadEventDetail, { immediate: true })

function goBack() {
  router.back()
}

function buildReminderOffsets() {
  const minutes = Number(form.value.reminderMinutes)
  return minutes > 0 ? [minutes] : []
}

function validateForm() {
  if (!form.value.title?.trim()) {
    ElMessage.warning('请输入标题')
    return false
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return false
  }
  if (!form.value.allDay && form.value.endTime <= form.value.startTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }
  return true
}

function buildPayload(force = false) {
  return {
    title: form.value.title.trim(),
    allDay: form.value.allDay,
    startTime: form.value.startTime,
    endTime: form.value.endTime,
    location: form.value.location?.trim() ?? '',
    note: form.value.note?.trim() ?? '',
    recurrence: form.value.recurrence,
    reminderOffsets: buildReminderOffsets(),
    force,
  }
}

async function saveEvent(force = false) {
  const id = route.params.id
  if (!id || !validateForm()) return

  saving.value = true
  try {
    const res = await updateEvent(id, buildPayload(force))
    if (!force && res.msg === '时间冲突' && Array.isArray(res.data) && res.data.length) {
      try {
        await ElMessageBox.confirm(
          '检测到时间冲突，是否仍然保存？',
          '时间冲突',
          { confirmButtonText: '仍然保存', cancelButtonText: '取消', type: 'warning' },
        )
        await saveEvent(true)
      } catch {
        /* 用户取消 */
      }
      return
    }
    ElMessage.success('修改已保存')
    await loadEventDetail(id)
  } catch {
    /* 错误已由请求拦截器提示 */
  } finally {
    saving.value = false
  }
}

async function onDeleteEvent() {
  const id = route.params.id
  if (!id) return

  try {
    await ElMessageBox.confirm(
      `确定删除「${form.value.title || '该事件'}」？`,
      '删除事件',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }

  deleting.value = true
  try {
    await deleteEvent(id)
    ElMessage.success('事件已删除')
    router.replace('/')
  } catch {
    /* 错误已由请求拦截器提示 */
  } finally {
    deleting.value = false
  }
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
    </header>

    <!-- 表单 -->
    <div v-loading="loading" class="form-body">
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
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="form-input form-date-picker"
            placeholder="请选择开始时间"
          />
        </div>
        <div class="time-field">
          <label class="form-label">结束</label>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="form-input form-date-picker"
            placeholder="请选择结束时间"
          />
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
      <button class="save-btn" :disabled="saving || deleting" @click="saveEvent(false)">
        {{ saving ? '保存中…' : '保存修改' }}
      </button>
      <button class="delete-btn" :disabled="saving || deleting" @click="onDeleteEvent">
        {{ deleting ? '删除中…' : '删除此事件' }}
      </button>
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

.form-date-picker {
  width: 100%;
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

.save-btn:disabled,
.delete-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
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
