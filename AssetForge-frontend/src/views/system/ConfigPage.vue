<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { configApi } from '@/api'

const loading = ref(false)
const configList = reactive([])

async function loadConfig() {
  loading.value = true
  try {
    const payload = await configApi.detail({})
    const records = payload?.data || []
    configList.splice(0, configList.length, ...(Array.isArray(records) ? records : []))
  } catch (error) {
    configList.splice(0, configList.length)
    ElMessage.error(error?.message || '系统配置加载失败')
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  try {
    await configApi.update(configList)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error(error?.message || '配置保存失败')
  }
}

loadConfig()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">系统配置</h1>
        <p class="page-desc">维护系统参数与基础配置项。</p>
      </div>
      <el-button type="primary" @click="saveConfig">保存配置</el-button>
    </div>

    <el-card shadow="never" class="page-card">
      <el-table :data="configList" stripe class="full-width" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="配置键" prop="configKey" min-width="240" />
        <el-table-column label="配置值" min-width="200">
          <template #default="{ row }">
            <el-input v-model="row.configValue" />
          </template>
        </el-table-column>
        <el-table-column label="说明" prop="description" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>
