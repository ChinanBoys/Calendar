<script setup>
import { onUnmounted, ref } from 'vue'
import { Microphone, EditPen, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseVoiceText } from '@/api/voice'
import { fetchEvents } from '@/api/events'
import { nowIso } from '@/utils/date'

const emit = defineEmits(['parsed', 'confirm'])

const recognizing = ref(false)
const parsing = ref(false)
const recognizedText = ref('')
const liveTranscript = ref('')

let activeRecognition = null
let finalTranscript = ''
let pointerIsDown = false
let shouldOpenEditorOnEnd = false
let editorOpening = false
let globalPressListenersAdded = false

function getSpeechRecognitionCtor() {
  if (typeof window === 'undefined') return null
  return window.SpeechRecognition || window.webkitSpeechRecognition || null
}

function createSpeechRecognition() {
  const SpeechRecognition = getSpeechRecognitionCtor()
  if (!SpeechRecognition) return null

  const recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = true
  recognition.interimResults = true
  recognition.maxAlternatives = 1
  return recognition
}

function normalizeSpeechText(text) {
  return (text || '').replace(/\s+/g, ' ').trim()
}

function currentTranscript() {
  return normalizeSpeechText(liveTranscript.value || finalTranscript)
}

function setRecognizedText(text) {
  const normalized = normalizeSpeechText(text)
  recognizedText.value = normalized ? `识别: "${normalized}"` : ''
}

function addGlobalPressListeners() {
  if (globalPressListenersAdded || typeof window === 'undefined') return
  window.addEventListener('pointerup', onVoicePressEnd)
  window.addEventListener('pointercancel', onVoicePressCancel)
  window.addEventListener('blur', onVoicePressCancel)
  globalPressListenersAdded = true
}

function removeGlobalPressListeners() {
  if (!globalPressListenersAdded || typeof window === 'undefined') return
  window.removeEventListener('pointerup', onVoicePressEnd)
  window.removeEventListener('pointercancel', onVoicePressCancel)
  window.removeEventListener('blur', onVoicePressCancel)
  globalPressListenersAdded = false
}

function resetRecognitionState() {
  pointerIsDown = false
  shouldOpenEditorOnEnd = false
  recognizing.value = false
  liveTranscript.value = ''
  activeRecognition = null
  removeGlobalPressListeners()
}

function bindRecognitionEvents(recognition) {
  recognition.onstart = () => {
    recognizing.value = true
    recognizedText.value = '正在听，请说话...'
  }

  recognition.onresult = (event) => {
    let interimTranscript = ''

    for (let i = event.resultIndex; i < event.results.length; i += 1) {
      const result = event.results[i]
      const transcript = result[0]?.transcript || ''
      if (result.isFinal) {
        finalTranscript += transcript
      } else {
        interimTranscript += transcript
      }
    }

    liveTranscript.value = normalizeSpeechText(`${finalTranscript}${interimTranscript}`)
    if (liveTranscript.value) {
      setRecognizedText(liveTranscript.value)
    }
  }

  recognition.onerror = (event) => {
    const errorTips = {
      'not-allowed': '麦克风权限未开启，请允许浏览器使用麦克风',
      'service-not-allowed': '浏览器阻止了语音识别服务，请检查权限设置',
      'audio-capture': '没有检测到可用麦克风',
      'no-speech': '没听清，请再说一次或手动补充',
      network: '语音识别服务连接失败，请稍后重试',
    }
    ElMessage.warning(errorTips[event.error] || '语音识别失败，请手动补充')
  }

  recognition.onend = async () => {
    const transcript = currentTranscript()
    const shouldOpenEditor = shouldOpenEditorOnEnd || pointerIsDown
    resetRecognitionState()

    if (shouldOpenEditor) {
      await openTranscriptEditor(transcript, {
        warnOnEmpty: true,
      })
    }
  }
}

async function openTranscriptEditor(defaultText = '', options = {}) {
  if (parsing.value || editorOpening) return

  editorOpening = true
  const inputValue = normalizeSpeechText(defaultText)

  if (!inputValue && options.warnOnEmpty) {
    ElMessage.warning('没听清，请在文本框里补充或重说')
  }

  try {
    const { value } = await ElMessageBox.prompt(
      options.message || '松开后已完成转写，你可以先修改文字再解析',
      options.title || '语音转写',
      {
        confirmButtonText: '解析',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '我明天下午三点到五点有个会议',
        inputValue,
        inputValidator: (value) => Boolean(value?.trim()) || '请输入要解析的内容',
        customClass: 'voice-text-editor',
      },
    )
    const text = normalizeSpeechText(value)
    if (!text) return

    setRecognizedText(text)
    await doParse(text)
  } catch {
    /* 取消 */
  } finally {
    editorOpening = false
  }
}

/** F-VOICE-01 — 长按语音按钮开始转写 */
async function onVoicePressStart(event) {
  event?.preventDefault()
  if (parsing.value || recognizing.value || editorOpening) return

  const recognition = createSpeechRecognition()
  if (!recognition) {
    ElMessage.warning('当前浏览器不支持语音识别，已切换到文本输入')
    await openTranscriptEditor('', {
      title: '文本输入',
      message: '当前环境无法直接语音转写，请手动输入日程描述',
      warnOnEmpty: false,
    })
    return
  }

  pointerIsDown = true
  shouldOpenEditorOnEnd = false
  finalTranscript = ''
  liveTranscript.value = ''
  activeRecognition = recognition
  bindRecognitionEvents(recognition)
  addGlobalPressListeners()
  if (event?.pointerId != null) {
    event.currentTarget?.setPointerCapture?.(event.pointerId)
  }

  try {
    recognition.start()
  } catch {
    resetRecognitionState()
    ElMessage.warning('语音识别启动失败，请使用文本输入')
    await openTranscriptEditor('', {
      title: '文本输入',
      message: '请输入自然语言描述你的日程',
      warnOnEmpty: false,
    })
  }
}

/** F-VOICE-01 — 松开语音按钮后结束转写，并弹出可编辑文本框 */
async function onVoicePressEnd(event) {
  event?.preventDefault()
  if (!pointerIsDown && !activeRecognition && !recognizing.value) return

  pointerIsDown = false
  shouldOpenEditorOnEnd = true
  removeGlobalPressListeners()

  if (!activeRecognition) {
    await openTranscriptEditor(currentTranscript(), {
      warnOnEmpty: true,
    })
    return
  }

  try {
    activeRecognition.stop()
  } catch {
    const transcript = currentTranscript()
    resetRecognitionState()
    await openTranscriptEditor(transcript, {
      warnOnEmpty: true,
    })
  }
}

function onVoicePressCancel(event) {
  event?.preventDefault?.()
  if (!pointerIsDown && !activeRecognition && !recognizing.value) return

  try {
    activeRecognition?.abort()
  } catch {
    /* ignore */
  } finally {
    resetRecognitionState()
    recognizedText.value = ''
  }
}

function onVoiceKeyDown(event) {
  if (event.repeat) return
  onVoicePressStart(event)
}

function onVoiceKeyUp(event) {
  onVoicePressEnd(event)
}

/** F-VOICE-02 — 文本兜底输入 */
async function onTextInput() {
  if (parsing.value || editorOpening) return

  await openTranscriptEditor('', {
    title: '文本输入',
    message: '输入自然语言描述你的日程',
    warnOnEmpty: false,
  })
}

/** F-VIEW-03 — 语音查询快捷「我有什么安排」 */
async function onScheduleQuery() {
  if (parsing.value || editorOpening) return
  const text = '我有什么安排'
  setRecognizedText(text)
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
    } else if (['create', 'update', 'delete'].includes(data.intent)) {
      emit('confirm', data)
    } else {
      ElMessage.info(`解析意图：${data.intent || 'unknown'}`)
    }

    emit('parsed', data)
  } catch {
    /* 错误已在 request 拦截器提示 */
  } finally {
    parsing.value = false
    recognizing.value = false
  }
}

/** 供父组件在「重说」后再次唤起输入修正 */
async function promptVoiceInput(defaultText = '') {
  await openTranscriptEditor(defaultText, {
    title: '重说',
    message: '请重新输入或修正语音内容',
    warnOnEmpty: false,
  })
}

defineExpose({ promptVoiceInput })

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

onUnmounted(() => {
  try {
    activeRecognition?.abort()
  } catch {
    /* ignore */
  } finally {
    resetRecognitionState()
  }
})
</script>

<template>
  <div class="voice-panel">
    <div v-if="recognizing" class="voice-panel__recognition recording-text">
      {{ liveTranscript || '正在听，请说话...' }}
    </div>
    <div v-else-if="recognizedText" class="voice-panel__recognition">
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
      :aria-pressed="recognizing"
      @pointerdown="onVoicePressStart"
      @pointercancel="onVoicePressCancel"
      @keydown.space="onVoiceKeyDown"
      @keyup.space="onVoiceKeyUp"
      @keydown.enter="onVoiceKeyDown"
      @keyup.enter="onVoiceKeyUp"
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

.voice-panel__recognition.recording-text {
  color: #ff7a59;
  font-weight: 600;
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
  touch-action: none;
  user-select: none;
  -webkit-user-select: none;
  position: relative;
}

.voice-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.voice-btn.recording {
  background: #ff7a59;
  box-shadow: 0 4px 16px rgba(255, 122, 89, 0.4);
}

.voice-btn.recording::before {
  content: '';
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px solid rgba(255, 122, 89, 0.35);
  animation: pulse 1.1s ease-out infinite;
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

@keyframes pulse {
  from {
    opacity: 0.8;
    transform: scale(0.92);
  }

  to {
    opacity: 0;
    transform: scale(1.18);
  }
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

:global(.voice-text-editor .el-textarea__inner) {
  min-height: 112px;
  resize: vertical;
  line-height: 1.6;
}
</style>
