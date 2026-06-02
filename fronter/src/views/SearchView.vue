<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Microphone, Location } from '@element-plus/icons-vue'
import { DEMO_SEARCH_RESULTS } from '@/mock/demoSearchResults'

const router = useRouter()
const keyword = ref('')

function goBack() {
  router.back()
}

function onResultClick(item) {
  router.push(`/events/${item.id}`)
}
</script>

<template>
  <div class="phone-frame">
    <!-- 搜索栏 -->
    <header class="search-header">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <div class="search-bar">
        <input
          v-model="keyword"
          class="search-input"
          type="search"
          placeholder="搜索日程..."
        />
        <button class="mic-btn" aria-label="语音搜索">
          <el-icon :size="18"><Microphone /></el-icon>
        </button>
      </div>
    </header>

    <!-- 结果列表 -->
    <div class="content">
      <h2 class="section-title">结果</h2>
      <div class="result-list">
        <div
          v-for="item in DEMO_SEARCH_RESULTS"
          :key="item.id"
          class="result-card"
          @click="onResultClick(item)"
        >
          <div class="result-card__datetime">
            <span class="result-date">{{ item.dateLabel }}</span>
            <span class="result-time">{{ item.time }}</span>
          </div>
          <div class="result-card__body">
            <div class="result-title">{{ item.title }}</div>
            <div v-if="item.location" class="result-location">
              <el-icon :size="13" color="#e5484d"><Location /></el-icon>
              <span>{{ item.location }}</span>
            </div>
          </div>
        </div>
      </div>
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
  background: #f5f7fa;
  border-radius: 36px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px 12px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  color: #333;
  cursor: pointer;
  border-radius: 50%;
  padding: 0;
  flex-shrink: 0;
}

.icon-btn:hover {
  background: #eee;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 12px 0 16px;
  background: #f5f6fa;
  border-radius: 20px;
  min-width: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  outline: none;
  min-width: 0;
}

.search-input::placeholder {
  color: #bbb;
}

.mic-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #5b6cff;
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

.mic-btn:hover {
  opacity: 0.8;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #999;
  margin-bottom: 10px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.result-card:hover {
  background: #fafbff;
}

.result-card__datetime {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 40px;
  gap: 2px;
}

.result-date {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.result-time {
  font-size: 12px;
  color: #999;
}

.result-card__body {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.result-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #888;
}
</style>
