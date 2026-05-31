<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchUpcomingReminders } from '@/api/reminders'
import { formatTime } from '@/utils/date'

const router = useRouter()
const reminders = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetchUpcomingReminders(24)
    reminders.value = res.data ?? []
  } catch {
    reminders.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="stub-page" v-loading="loading">
    <el-button text @click="router.back()">← 返回</el-button>
    <h2>提醒中心</h2>
    <p class="api-hint">F-REMIND-01 · GET /api/reminders/upcoming</p>
    <div v-if="reminders.length" class="list">
      <div v-for="item in reminders" :key="item.id" class="item">
        <strong>{{ item.eventTitle }}</strong>
        <span>{{ formatTime(item.fireTime) }} 提醒</span>
      </div>
    </div>
    <p v-else-if="!loading" class="empty">暂无即将到来的提醒</p>
  </div>
</template>

<style scoped>
.stub-page { padding: 24px; max-width: 400px; margin: 0 auto; }
.api-hint { color: #999; font-size: 13px; margin-bottom: 16px; }
.item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}
.empty { color: #aaa; text-align: center; margin-top: 24px; }
</style>
