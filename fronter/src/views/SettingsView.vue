<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchSettings, clearVoiceLogs } from '@/api/settings'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const settings = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetchSettings()
    settings.value = res.data
  } catch {
    settings.value = null
  } finally {
    loading.value = false
  }
})

async function onClearVoiceLogs() {
  try {
    await ElMessageBox.confirm('确定清除全部语音数据？', '隐私', { type: 'warning' })
    await clearVoiceLogs()
    ElMessage.success('已清除')
  } catch {
    /* cancel */
  }
}
</script>

<template>
  <div class="stub-page" v-loading="loading">
    <el-button text @click="router.back()">← 返回</el-button>
    <h2>设置</h2>
    <p class="api-hint">F-SET · GET/PUT /api/settings</p>
    <template v-if="settings">
      <div class="setting-row">
        <span>时区</span>
        <span>{{ settings.timezone }}</span>
      </div>
      <div class="setting-row">
        <span>默认提醒</span>
        <span>提前 {{ settings.defaultReminderMinutes }} 分钟</span>
      </div>
      <div class="setting-row">
        <span>通知</span>
        <span>{{ settings.notifyEnabled ? '开启' : '关闭' }}</span>
      </div>
    </template>
    <el-button type="danger" plain style="margin-top: 24px; width: 100%" @click="onClearVoiceLogs">
      清除全部语音数据
    </el-button>
  </div>
</template>

<style scoped>
.stub-page { padding: 24px; max-width: 400px; margin: 0 auto; }
.api-hint { color: #999; font-size: 13px; margin-bottom: 16px; }
.setting-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}
</style>
