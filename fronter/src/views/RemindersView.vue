<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft, AlarmClock } from '@element-plus/icons-vue'
import { DEMO_UPCOMING_REMINDERS, DEMO_HISTORY_REMINDERS } from '@/mock/demoReminders'

const router = useRouter()

function goBack() {
  router.back()
}

function onItemClick(item) {
  if (item.eventId) {
    router.push(`/events/${item.eventId}`)
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
      <span class="top-bar__title">提醒</span>
      <span class="top-bar__spacer" />
    </header>

    <!-- 内容区 -->
    <div class="content">
      <!-- 即将到来 -->
      <section class="section">
        <h2 class="section-title">即将到来</h2>
        <div class="card-list">
          <div
            v-for="item in DEMO_UPCOMING_REMINDERS"
            :key="item.id"
            class="reminder-card"
            @click="onItemClick(item)"
          >
            <div class="reminder-card__icon">
              <el-icon :size="20" color="#e5484d"><AlarmClock /></el-icon>
            </div>
            <div class="reminder-card__body">
              <div class="reminder-card__title">{{ item.title }}</div>
              <div class="reminder-card__subtitle">{{ item.subtitle }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 历史提醒 -->
      <section class="section">
        <h2 class="section-title">历史提醒</h2>
        <div class="card-list">
          <div
            v-for="item in DEMO_HISTORY_REMINDERS"
            :key="item.id"
            class="reminder-card reminder-card--history"
            @click="onItemClick(item)"
          >
            <div class="reminder-card__icon">
              <el-icon :size="20" color="#e5484d"><AlarmClock /></el-icon>
            </div>
            <div class="reminder-card__body">
              <div class="reminder-card__title">{{ item.title }}</div>
              <div class="reminder-card__subtitle">{{ item.subtitle }}</div>
            </div>
          </div>
        </div>
      </section>
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

.top-bar {
  display: grid;
  grid-template-columns: 36px 1fr 36px;
  align-items: center;
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
}

.icon-btn:hover {
  background: #eee;
}

.top-bar__title {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.top-bar__spacer {
  width: 36px;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #999;
  margin-bottom: 10px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reminder-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background 0.2s;
}

.reminder-card:hover {
  background: #fafbff;
}

.reminder-card--history {
  opacity: 0.85;
}

.reminder-card__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-top: 2px;
}

.reminder-card__body {
  flex: 1;
  min-width: 0;
}

.reminder-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
  margin-bottom: 4px;
}

.reminder-card__subtitle {
  font-size: 13px;
  color: #999;
  line-height: 1.3;
}
</style>
