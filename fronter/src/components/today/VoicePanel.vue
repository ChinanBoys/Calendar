<script setup>
import { ref } from 'vue'
import { Microphone, EditPen, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseVoiceText } from '@/api/voice'
import { fetchEvents } from '@/api/events'
import { nowIso } from '@/utils/date'

const emit = defineEmits(['parsed'])

const recognizing = ref(false)
const parsing = ref(false)
const recognizedText = ref('识别: "我明天下午三点到五点有个会议"')

/** F-VOICE-01 — 语音按钮（演示：弹出输入模拟 ASR 转写） */
async function onVoiceClick() {
  if (parsing.value) return

  try {
    const { value } = await ElMessageBox.prompt(
      '按住说话效果演示：请输入语音识别文本',
      '语音输入',
      {
        confirmButtonText: '解析',
        cancelButtonText: '取消',
        inputPlaceholder: '我明天下午三点到五点有个会议',
        inputValue: recognizedText.value.replace(/^识别:\s*"?|"?$/g, '') || '',
      },
    )
    if (!value?.trim()) return
    recognizedText.value = `识别: "${value.trim()}"`
    await doParse(value.trim())
  } catch {
    /* 取消 */
  }
}

/** F-VOICE-02 — 文本兜底输入 */
async function onTextInput() {
  if (parsing.value) return

  try {
    const { value } = await ElMessageBox.prompt(
      '输入自然语言描述你的日程',
      '文本输入',
      {
        confirmButtonText: '解析',
        cancelButtonText: '取消',
        inputPlaceholder: '明天下午3点在3号会议室开会',
      },
    )
    if (!value?.trim()) return
    recognizedText.value = `识别: "${value.trim()}"`
    await doParse(value.trim())
  } catch {
    /* 取消 */
  }
}

/** F-VIEW-03 — 语音查询快捷「我有什么安排」 */
async function onScheduleQuery() {
  if (parsing.value) return
  const text = '我有什么安排'
  recognizedText.value = `识别: "${text}"`
  await doParse(text)
}

async function doParse(text) {
  parsing.value = true
  try {
    const res = await parseVoiceText({
      text,
      now: nowIso(),
      tz: 'Asia/Shanghai',
    })
    const data = res.data

    if (data.intent === 'query') {
      await handleQueryIntent(data)
    } else if (data.intent === 'create') {
      ElMessage.success(`已解析：${data.title || '新事件'}，确认卡功能待 P2 页面实现`)
    } else {
      ElMessage.info(`解析意图：${data.intent}`)
    }

    emit('parsed', data)
  } catch {
    /* 错误已在拦截器提示 */
  } finally {
    parsing.value = false
    recognizing.value = false
  }
}

/** F-VIEW-03 — 查询意图 → GET /api/events */
async function handleQueryIntent(parseResult) {
  try {
    const params = {
      page: 1,
      pageSize: 20,
    }
    if (parseResult.queryStart) params.from = parseResult.queryStart
    if (parseResult.queryEnd) params.to = parseResult.queryEnd

    const res = await fetchEvents(params)
    const rows = res.data?.rows ?? []
    if (rows.length === 0) {
      ElMessage.info('该时段暂无安排')
    } else {
      const summary = rows.map((e) => e.title).join('、')
      ElMessage.success(`共 ${rows.length} 项：${summary}`)
    }
  } catch {
    /* handled */
  }
}
</script>

<template>
  <div class="voice-panel">
    <div v-if="recognizedText" class="voice-panel__recognition">
      {{ recognizedText }}
    </div>
    <div v-else class="voice-panel__hint">
      按住说话，如：明天3点开会
    </div>

    <button
      class="voice-btn"
      :class="{ recording: recognizing, loading: parsing }"
      :disabled="parsing"
      aria-label="语音输入"
      @click="onVoiceClick"
    >
      <span v-if="parsing" class="voice-btn__spinner" />
      <el-icon v-else :size="32"><Microphone /></el-icon>
    </button>

    <div class="voice-panel__actions">
      <button class="action-btn" :disabled="parsing" @click="onTextInput">
        <el-icon :size="16"><EditPen /></el-icon>
        输入
      </button>
      <button class="action-btn" :disabled="parsing" @click="onScheduleQuery">
        <el-icon :size="16"><MagicStick /></el-icon>
        我有什么安排
      </button>
    </div>
  </div>
</template>

<style scoped>
.voice-panel {
  flex-shrink: 0;
  padding: 12px 16px 24px;
  background: linear-gradient(to top, #fff 80%, transparent);
  border-top: 1px solid #f0f0f0;
}

.voice-panel__recognition,
.voice-panel__hint {
  text-align: center;
  font-size: 13px;
  color: #aaa;
  margin-bottom: 12px;
  min-height: 20px;
  padding: 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.voice-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border: none;
  border-radius: 50%;
  background: #5b6cff;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(91, 108, 255, 0.4);
  transition: background 0.2s, transform 0.15s;
}

.voice-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.voice-btn.recording {
  background: #ff7a59;
  box-shadow: 0 4px 16px rgba(255, 122, 89, 0.4);
}

.voice-btn.loading {
  opacity: 0.85;
  cursor: wait;
}

.voice-btn__spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.voice-panel__actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  background: #fff;
  color: #555;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
