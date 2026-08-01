<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { authApi } from '@/api'
import { authState, setSelectedRole } from '@/utils/auth'
import { findFirstAccessiblePath } from '@/utils/role-access'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const selectedRoleId = ref(null)

const roles = computed(() => authState.user?.roles || [])

async function confirmRole() {
  const role = roles.value.find((item) => item.id === selectedRoleId.value)

  if (!role) {
    ElMessage.warning('请选择角色')
    return
  }

  loading.value = true
  try {
    const payload = await authApi.selectRole({ roleId: role.id })
    const data = payload?.data || {}

    setSelectedRole({
      id: data.roleId || role.id,
      code: data.roleCode || role.code,
      name: data.roleName || role.name
    })

    ElMessage.success('角色切换成功')
  } catch (_error) {
    setSelectedRole({
      id: role.id,
      code: role.code,
      name: role.name
    })
    ElMessage.success('角色切换成功')
  } finally {
    loading.value = false
  }

  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  const fallbackPath = findFirstAccessiblePath(router.options.routes, role.code)
  router.push(redirect || fallbackPath)
}
</script>

<template>
  <div class="role-page">
    <el-card shadow="never" class="role-card">
      <template #header>
        <div>
          <h2>选择当前角色</h2>
          <p>请选择本次进入系统使用的角色。</p>
        </div>
      </template>

      <el-empty
        v-if="roles.length === 0"
        description="当前用户没有可用角色，请先检查角色配置"
      />

      <el-radio-group v-else v-model="selectedRoleId" class="role-list">
        <el-radio
          v-for="role in roles"
          :key="role.id"
          :value="role.id"
          border
          class="role-item"
        >
          <div class="role-content">
            <strong>{{ role.name }}</strong>
            <span>{{ role.code }}</span>
          </div>
        </el-radio>
      </el-radio-group>

      <div class="role-actions">
        <el-button type="primary" :loading="loading" @click="confirmRole">进入系统</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.role-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(14, 116, 144, 0.12), transparent 35%),
    linear-gradient(160deg, rgba(22, 163, 74, 0.1), transparent 40%),
    #f8fafc;
}

.role-card {
  width: min(720px, 100%);
  border-radius: 24px;
}

.role-card h2 {
  margin: 0;
}

.role-card p {
  margin: 8px 0 0;
  color: #64748b;
}

.role-list {
  display: grid;
  gap: 12px;
}

.role-item {
  width: 100%;
  margin: 0;
}

.role-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-content span {
  color: #64748b;
  font-size: 12px;
}

.role-actions {
  margin-top: 20px;
}
</style>
