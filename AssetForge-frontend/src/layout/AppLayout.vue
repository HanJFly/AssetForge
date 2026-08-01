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
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
}

.app-aside {
  position: sticky;
  top: 0;
  height: 100vh;
  background: linear-gradient(180deg, #0f172a, #1e293b);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.app-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.app-content {
  padding: 24px;
}

@media (max-width: 960px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .app-aside {
    position: relative;
    height: auto;
  }
}
</style>
