<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchEventById } from '@/api/events'
import { formatTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const event = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetchEventById(route.params.id)
    event.value = res.data
  } catch {
    event.value = null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="stub-page" v-loading="loading">
    <el-button text @click="router.back()">← 返回</el-button>
    <h2>事件详情</h2>
    <p>F-VIEW-04 · GET /api/events/{id}</p>
    <template v-if="event">
      <div class="detail-card">
        <h3>{{ event.title }}</h3>
        <p>{{ formatTime(event.startTime) }} - {{ formatTime(event.endTime) }}</p>
        <p v-if="event.location">{{ event.location }}</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.stub-page { padding: 24px; max-width: 400px; margin: 0 auto; }
.detail-card {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9ff;
  border-radius: 12px;
  text-align: left;
}
</style>
