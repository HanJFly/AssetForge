<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { authApi } from '@/api'
import { restoreUser, setSession } from '@/utils/auth'

const router = useRouter()

const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  try {
    const payload = await authApi.login(form)
    const loginData = payload?.data || {}
    const token = loginData.token || loginData.accessToken
    const user = Array.isArray(loginData.user) ? loginData.user[0] : loginData.user

    if (!token) {
      throw new Error('登录接口未返回 token')
    }

    setSession({ token, user: user || null })

    if (!user) {
      await restoreUser()
    }

    ElMessage.success('登录成功')
    router.push('/select-role')
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '登录失败')
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="hero">
        <span class="chip">Asset Lifecycle Control</span>
        <h1>AssetForge</h1>
        <p>统一资产全生命周期管理入口。</p>
      </div>

      <el-card shadow="never" class="login-card">
        <template #header>
          <div>
            <h3>系统登录</h3>
            <p>登录后请选择当前使用角色。</p>
          </div>
        </template>

        <el-form label-position="top">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
        </el-form>

        <el-button type="primary" class="full-width" size="large" @click="handleLogin">
          登录
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(14, 116, 144, 0.18), transparent 35%),
    linear-gradient(160deg, rgba(22, 163, 74, 0.14), transparent 40%),
    #eaf0f8;
}

.login-panel {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.2fr 420px;
  gap: 24px;
  align-items: center;
}

.hero {
  padding: 24px;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 700;
}

.hero h1 {
  margin: 18px 0 12px;
  font-size: 60px;
  line-height: 1;
  color: #0f172a;
}

.hero p {
  margin: 0;
  max-width: 560px;
  line-height: 1.8;
  color: #475569;
}

.login-card {
  border-radius: 24px;
}

.login-card h3 {
  margin: 0;
}

.login-card p {
  margin: 8px 0 0;
  color: #64748b;
}

.full-width {
  width: 100%;
}

@media (max-width: 960px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .hero h1 {
    font-size: 44px;
  }
}
</style>
