<script setup>
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { DEMO_SETTINGS } from '@/mock/demoSettings'

const router = useRouter()

function goBack() {
  router.back()
}
</script>

<template>
  <div class="phone-frame">
    <!-- 顶栏 -->
    <header class="top-bar">
      <button class="icon-btn" aria-label="返回" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <span class="top-bar__title">设置</span>
      <span class="top-bar__spacer" />
    </header>

    <!-- 设置列表 -->
    <div class="content">
      <!-- 提醒 -->
      <section class="section">
        <h2 class="section-title">提醒</h2>
        <div class="cell-group">
          <div class="cell">
            <span class="cell__label">默认提前量</span>
            <span class="cell__value">
              {{ DEMO_SETTINGS.defaultReminderMinutes }}分钟
              <el-icon :size="14" class="cell__arrow"><ArrowRight /></el-icon>
            </span>
          </div>
          <div class="cell">
            <span class="cell__label">通知开关</span>
            <span class="cell__value cell__value--status">
              <span class="status-dot" />
              已开启
            </span>
          </div>
        </div>
      </section>

      <!-- 通用 -->
      <section class="section">
        <h2 class="section-title">通用</h2>
        <div class="cell-group">
          <div class="cell">
            <span class="cell__label">时区</span>
            <span class="cell__value">
              {{ DEMO_SETTINGS.timezone }}
              <el-icon :size="14" class="cell__arrow"><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </section>

      <!-- 隐私 -->
      <section class="section">
        <h2 class="section-title">隐私</h2>
        <div class="cell-group">
          <div class="cell">
            <span class="cell__label">语音原文保留</span>
            <span class="cell__value">
              {{ DEMO_SETTINGS.voiceRetention }}
              <el-icon :size="14" class="cell__arrow"><ArrowRight /></el-icon>
            </span>
          </div>
          <div class="cell">
            <span class="cell__label">清除全部语音数据</span>
            <span class="cell__value">
              <el-icon :size="14" class="cell__arrow"><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </section>

      <!-- 账户 -->
      <section class="section">
        <h2 class="section-title">账户</h2>
        <div class="cell-group">
          <div class="cell">
            <span class="cell__label">登录 / 多端同步</span>
            <span class="cell__value">
              <el-icon :size="14" class="cell__arrow"><ArrowRight /></el-icon>
            </span>
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
  margin-bottom: 20px;
}

.section-title {
  font-size: 13px;
  font-weight: 500;
  color: #999;
  margin-bottom: 8px;
  padding-left: 4px;
}

.cell-group {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.cell:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.cell:hover {
  background: #fafbff;
}

.cell__label {
  font-size: 15px;
  color: #333;
}

.cell__value {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #999;
}

.cell__value--status {
  color: #52c41a;
  font-size: 14px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
  margin-right: 2px;
}

.cell__arrow {
  color: #ccc;
  margin-left: 2px;
}
</style>
