<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { authApi } from '@/api'
import { authState, clearSession } from '@/utils/auth'

const router = useRouter()

const props = defineProps({
  title: {
    type: String,
    default: 'AssetForge'
  }
})

const displayName = computed(() => {
  return authState.user?.realName || authState.user?.username || '未登录'
})

const displayRole = computed(() => {
  return authState.selectedRole?.name || '未选择角色'
})

async function handleLogout() {
  try {
    await authApi.logout({})
  } catch (_error) {
    // 当前后端可以不实现退出接口，这里仍然执行前端退出。
  } finally {
    clearSession()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

function switchRole() {
  router.push('/select-role')
}
</script>

<template>
  <header class="topbar">
    <div>
      <p class="eyebrow">AssetForge Frontend</p>
      <h2>{{ props.title }}</h2>
    </div>

    <div class="topbar-right">
      <el-tag type="success" effect="dark">{{ displayRole }}</el-tag>
      <span class="user-name">{{ displayName }}</span>
      <el-avatar size="default">{{ displayName.slice(0, 1).toUpperCase() }}</el-avatar>
      <el-button text @click="switchRole">切换角色</el-button>
      <el-button text @click="handleLogout">退出</el-button>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 20px 24px 0;
}

.eyebrow {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.topbar h2 {
  margin: 6px 0 0;
  font-size: 28px;
  color: #0f172a;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #334155;
  font-size: 14px;
}
</style>
