<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'
import { authState, restoreUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const currentTitle = computed(() => route.meta.title || 'AssetForge')

async function restoreCurrentUser() {
  if (!authState.token) return

  try {
    await restoreUser()
  } catch (_error) {
    ElMessage.warning('登录状态已失效，请重新登录')
    router.push('/login')
  }
}

onMounted(restoreCurrentUser)
</script>

<template>
  <div class="app-shell">
    <aside class="app-aside">
      <AppSidebar />
    </aside>

    <div class="app-main">
      <AppTopbar :title="currentTitle" />

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f3f7fb;
}

.app-aside {
  position: fixed;
  top: 0;
  left: 0;
  width: 260px;
  height: 100vh;
  background: linear-gradient(180deg, #0f172a, #1e293b);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 20;
  overflow-y: auto;
}

.app-main {
  margin-left: 260px;
  min-height: 100vh;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.app-content {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 960px) {
  .app-aside {
    position: relative;
    left: auto;
    width: 100%;
    height: auto;
    overflow: visible;
  }

  .app-main {
    margin-left: 0;
  }
}
</style>
