<script setup>
import { ref, computed, watch } from 'vue'
import { Close, Microphone, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkConflicts, createEvent, updateEvent, deleteEvent } from '@/api/events'
import { formatTime } from '@/utils/date'

const props = defineProps({
  visible: { type: Boolean, default: false },
  parseResult: { type: Object, default: null },
})

const emit = defineEmits(['update:visible', 'saved', 'retalk', 'cancel'])

const form = ref({})
const conflicts = ref([])
const saving = ref(false)
const editMode = ref(false)

const RECURRENCE_LABELS = {
  none: '不重复',
  daily: '每天',
  weekly: '每周',
  monthly: '每月',
}

const LOW_CONFIDENCE = 0.6

watch(
  () => props.parseResult,
  (data) => {
    if (!data) return
    form.value = {
      title: data.title ?? '',
      startTime: data.startTime ?? '',
      endTime: data.endTime ?? '',
      allDay: data.allDay ?? false,
      location: data.location ?? '',
      reminderOffsets: data.reminderOffsets ?? [15],
      recurrence: data.recurrence ?? 'none',
      rawText: data.rawText ?? '',
      intent: data.intent ?? 'create',
      matchKeyword: data.matchKeyword ?? '',
      confidence: data.confidence ?? {},
      targetEventId: data.targetEventId ?? null,
    }
    editMode.value = false
    loadConflicts()
  },
  { immediate: true },
)

watch(
  () => [form.value.startTime, form.value.endTime, props.visible],
  () => {
    if (props.visible && form.value.startTime && form.value.endTime) {
      loadConflicts()
    }
  },
)

const dateLabel = computed(() => {
  if (!form.value.startTime) return '—'
  const d = new Date(form.value.startTime)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${month}月${day}日 ${weekdays[d.getDay()]}`
})

const reminderLabel = computed(() => {
  const offsets = form.value.reminderOffsets
  if (!offsets?.length) return '不提醒'
  return offsets.map((m) => `提前${m}分钟`).join('、')
})

const recurrenceLabel = computed(() => RECURRENCE_LABELS[form.value.recurrence] ?? '不重复')

const locationDisplay = computed(() => {
  if (form.value.location) return form.value.location
  return '（未提及，请核对）'
})

function isLowConfidence(field) {
  const c = form.value.confidence?.[field]
  if (field === 'location' && !form.value.location) return true
  return c !== undefined && c < LOW_CONFIDENCE
}

function formatConflict(c) {
  return `「${c.title} ${formatTime(c.startTime)}-${formatTime(c.endTime)}」`
}

const conflictMessage = computed(() => {
  if (!conflicts.value.length) return ''
  const list = conflicts.value.map(formatConflict).join('、')
  return `与 ${list} 时间冲突，可调整或仍然保存`
})

/** F-CONFLICT-01 — 冲突检测 */
async function loadConflicts() {
  if (!form.value.startTime || !form.value.endTime) {
    conflicts.value = []
    return
  }
  try {
    const params = {
      startTime: form.value.startTime,
      endTime: form.value.endTime,
    }
    if (form.value.targetEventId) params.excludeId = form.value.targetEventId
    const res = await checkConflicts(params)
    conflicts.value = res.data ?? []
  } catch {
    conflicts.value = []
  }
}

function close() {
  emit('update:visible', false)
  emit('cancel')
}

/** F-VOICE-04 — 重说 */
function onRetalk() {
  emit('retalk')
  close()
}

/** F-EDIT-02 — 手动修改 */
function onManualEdit() {
  editMode.value = true
}

async function onEditSave() {
  editMode.value = false
  await loadConflicts()
}

/** F-ADD-01 — 确认保存 */
async function onConfirmSave(force = false) {
  if (!form.value.title || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请完善标题与时间')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.value.title,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      allDay: form.value.allDay,
      location: form.value.location || undefined,
      recurrence: form.value.recurrence,
      reminderOffsets: form.value.reminderOffsets,
      force,
    }

    if (form.value.intent === 'update' && form.value.targetEventId) {
      await updateEvent(form.value.targetEventId, { ...payload, force })
      ElMessage.success('修改已保存')
    } else if (form.value.intent === 'delete' && form.value.targetEventId) {
      await deleteEvent(form.value.targetEventId)
      ElMessage.success('事件已删除')
    } else {
      const res = await createEvent(payload)
      const data = res.data
      if (data?.created === false && data?.conflicts?.length) {
        conflicts.value = data.conflicts
        try {
          await ElMessageBox.confirm(
            conflictMessage.value || '检测到时间冲突，是否仍然保存？',
            '时间冲突',
            { confirmButtonText: '仍然保存', cancelButtonText: '取消', type: 'warning' },
          )
          await onConfirmSave(true)
          return
        } catch {
          return
        }
      }
      ElMessage.success('日程已保存')
    }

    emit('saved')
    close()
  } catch {
    /* handled by interceptor */
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <Transition name="fade">
    <div v-if="visible" class="confirm-overlay" @click.self="close">
      <Transition name="slide-up">
        <div v-if="visible" class="confirm-card">
            <!-- 标题栏 -->
            <div class="confirm-header">
              <span class="confirm-title">确认这条日程</span>
              <button class="close-btn" aria-label="关闭" @click="close">
                <el-icon :size="18"><Close /></el-icon>
              </button>
            </div>

            <div class="confirm-body">
              <!-- F-CONFLICT-01 冲突提示 -->
              <div v-if="conflicts.length" class="conflict-bar">
                <el-icon :size="16"><WarningFilled /></el-icon>
                <span>{{ conflictMessage }}</span>
              </div>

              <!-- F-VOICE-03 原话 -->
              <div class="raw-text">
                原话：{{ form.rawText || '—' }}
              </div>

              <!-- 手动编辑模式 F-EDIT-02 -->
              <template v-if="editMode">
                <el-form label-width="56px" class="edit-form">
                  <el-form-item label="标题">
                    <el-input v-model="form.title" />
                  </el-form-item>
                  <el-form-item label="开始">
                    <el-date-picker
                      v-model="form.startTime"
                      type="datetime"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                      style="width: 100%"
                    />
                  </el-form-item>
                  <el-form-item label="结束">
                    <el-date-picker
                      v-model="form.endTime"
                      type="datetime"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                      style="width: 100%"
                    />
                  </el-form-item>
                  <el-form-item label="地点">
                    <el-input v-model="form.location" placeholder="请输入地点" />
                  </el-form-item>
                  <el-form-item label="提醒">
                    <el-select v-model="form.reminderOffsets" multiple style="width: 100%">
                      <el-option label="提前5分钟" :value="5" />
                      <el-option label="提前15分钟" :value="15" />
                      <el-option label="提前30分钟" :value="30" />
                      <el-option label="提前60分钟" :value="60" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="重复">
                    <el-select v-model="form.recurrence" style="width: 100%">
                      <el-option label="不重复" value="none" />
                      <el-option label="每天" value="daily" />
                      <el-option label="每周" value="weekly" />
                      <el-option label="每月" value="monthly" />
                    </el-select>
                  </el-form-item>
                </el-form>
                <el-button type="primary" style="width: 100%; margin-bottom: 8px" @click="onEditSave">
                  完成修改
                </el-button>
              </template>

              <!-- F-VOICE-03 字段展示 -->
              <template v-else>
                <div class="field-list">
                  <div class="field-row">
                    <span class="field-label">标题</span>
                    <span
                      class="field-value"
                      :class="{ 'low-confidence': isLowConfidence('title') }"
                    >{{ form.title || '—' }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">日期</span>
                    <span class="field-value">{{ dateLabel }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">开始</span>
                    <span
                      class="field-value"
                      :class="{ 'low-confidence': isLowConfidence('startTime') }"
                    >{{ formatTime(form.startTime) || '—' }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">结束</span>
                    <span
                      class="field-value"
                      :class="{ 'low-confidence': isLowConfidence('endTime') }"
                    >{{ formatTime(form.endTime) || '—' }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">地点</span>
                    <span
                      class="field-value"
                      :class="{ 'low-confidence': isLowConfidence('location') }"
                    >{{ locationDisplay }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">提醒</span>
                    <span class="field-value">{{ reminderLabel }}</span>
                  </div>
                  <div class="field-row">
                    <span class="field-label">重复</span>
                    <span class="field-value">{{ recurrenceLabel }}</span>
                  </div>
                </div>
              </template>
            </div>

            <!-- 操作按钮 -->
            <div v-if="!editMode" class="confirm-actions">
              <div class="action-row">
                <button class="secondary-btn" @click="onRetalk">
                  <el-icon :size="14"><Microphone /></el-icon>
                  重说
                </button>
                <button class="secondary-btn" @click="onManualEdit">手动修改</button>
              </div>
              <button
                class="primary-btn"
                :disabled="saving"
                @click="onConfirmSave(false)"
              >
                {{ saving ? '保存中…' : '确认保存' }}
              </button>
              <button class="cancel-btn" @click="close">取消</button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
</template>

<style scoped>
.confirm-overlay {
  position: absolute;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
  border-radius: 36px;
}

.confirm-card {
  width: 100%;
  max-height: 85vh;
  background: #fff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
}

.confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.confirm-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
}

.close-btn:hover {
  background: #f5f5f5;
}

.confirm-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.conflict-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #fff0f0;
  border: 1px dashed #e5484d;
  border-radius: 8px;
  font-size: 13px;
  color: #e5484d;
  line-height: 1.4;
}

.raw-text {
  padding: 10px 12px;
  margin-bottom: 16px;
  background: #f8f9ff;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.field-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.field-row {
  display: flex;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid #f5f5f5;
}

.field-row:last-child {
  border-bottom: none;
}

.field-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 14px;
  color: #999;
}

.field-value {
  flex: 1;
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
}

.field-value.low-confidence {
  color: #b8860b;
  background: #fff8e6;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px dashed #f5a623;
}

.edit-form {
  margin-bottom: 8px;
}

.confirm-actions {
  padding: 12px 16px 24px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.action-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.secondary-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  background: #fff;
  color: #555;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.secondary-btn:hover {
  background: #f5f5f5;
}

.primary-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 22px;
  background: #5b6cff;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 8px;
  transition: opacity 0.2s;
}

.primary-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  width: 100%;
  height: 36px;
  border: none;
  background: transparent;
  color: #999;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn:hover {
  color: #666;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}
</style>
