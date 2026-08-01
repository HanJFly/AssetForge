<script setup>
defineProps({
  title: String,
  value: [String, Number],
  desc: String,
  accent: {
    type: String,
    default: '#2563eb'
  }
})
</script>

<template>
  <el-card shadow="never" class="page-card stat-card">
    <div class="stat-top">
      <span class="accent" :style="{ background: accent }"></span>
      <p>{{ title }}</p>
    </div>
    <h3>{{ value }}</h3>
    <span class="desc">{{ desc }}</span>
  </el-card>
</template>

<style scoped>
.stat-card {
  min-height: 148px;
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.accent {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.stat-top p {
  margin: 0;
  color: #475569;
}

h3 {
  margin: 18px 0 10px;
  font-size: 34px;
  color: #0f172a;
}

.desc {
  color: #64748b;
}
</style>
