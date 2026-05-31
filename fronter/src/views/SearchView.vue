<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { fetchEvents } from '@/api/events'

const router = useRouter()
const keyword = ref('')
const results = ref([])
const loading = ref(false)

async function onSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res = await fetchEvents({ keyword: keyword.value.trim(), page: 1, pageSize: 20 })
    results.value = res.data?.rows ?? []
  } catch {
    results.value = []
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="stub-page">
    <el-button text @click="router.back()">← 返回</el-button>
    <h2>搜索</h2>
    <p class="api-hint">F-VIEW-05 · GET /api/events?keyword=</p>
    <el-input
      v-model="keyword"
      placeholder="搜索事件..."
      :prefix-icon="Search"
      clearable
      @keyup.enter="onSearch"
    />
    <el-button type="primary" :loading="loading" style="margin-top: 12px; width: 100%" @click="onSearch">
      搜索
    </el-button>
    <div v-if="results.length" class="results">
      <div v-for="item in results" :key="item.id" class="result-item">
        {{ item.title }} · {{ item.location || '无地点' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.stub-page { padding: 24px; max-width: 400px; margin: 0 auto; }
.api-hint { color: #999; font-size: 13px; margin-bottom: 16px; }
.results { margin-top: 16px; }
.result-item {
  padding: 12px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}
</style>
