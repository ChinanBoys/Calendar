<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <RouterView v-slot="{ Component, route }">
      <component :is="Component" v-if="route.name === 'today'" />
      <div v-else class="page-wrap">
        <component :is="Component" />
      </div>
    </RouterView>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #eef0f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.page-wrap {
  width: 375px;
  min-height: 600px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
</style>
