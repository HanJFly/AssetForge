<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { authState } from '@/utils/auth'
import { getVisibleMenus } from '@/utils/role-access'

const router = useRouter()
const route = useRoute()

const activePath = computed(() => route.path)
const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const visibleMenus = computed(() => getVisibleMenus(selectedRoleCode.value))

function navigate(path) {
  router.push(path)
}
</script>

<template>
  <div class="sidebar">
    <div class="brand">
      <div class="brand-badge">AF</div>
      <div>
        <h1>AssetForge</h1>
        <p>实物资产管理前端</p>
      </div>
    </div>

    <div class="menu-groups">
      <section v-for="group in visibleMenus" :key="group.key" class="menu-group">
        <p class="group-title">{{ group.title }}</p>
        <button
          v-for="item in group.items"
          :key="item.path"
          type="button"
          class="menu-item"
          :class="{ active: activePath === item.path }"
          @click="navigate(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </button>
      </section>
    </div>
  </div>
</template>

<style scoped>
.sidebar {
  height: 100%;
  padding: 22px 18px;
  color: #dbeafe;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.brand-badge {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(135deg, #38bdf8, #10b981);
  color: #0f172a;
}

.brand h1 {
  margin: 0;
  font-size: 20px;
}

.brand p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.menu-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.group-title {
  margin: 0 0 8px;
  color: #94a3b8;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu-item {
  border: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 12px;
  border-radius: 14px;
  background: transparent;
  color: inherit;
  transition: all 0.2s ease;
}

.menu-item:hover,
.menu-item.active {
  background: rgba(56, 189, 248, 0.14);
  color: #ffffff;
}
</style>
