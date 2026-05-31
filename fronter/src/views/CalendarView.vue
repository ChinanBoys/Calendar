<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="stub-page">
    <el-button text @click="router.back()">← 返回</el-button>
    <h2>日历视图（周/月）</h2>
    <p>F-VIEW-02 · GET /api/events</p>
    <p class="hint">页面待后续实现</p>
  </div>
</template>

<style scoped>
.stub-page {
  padding: 24px;
  text-align: center;
}
.hint { color: #999; margin-top: 16px; }
</style>
