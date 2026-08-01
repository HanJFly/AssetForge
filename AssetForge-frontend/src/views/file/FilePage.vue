<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { fileApi } from '@/api'
import { authState } from '@/utils/auth'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const loading = ref(false)
const bindForm = reactive({
  bizType: 'SCRAP',
  bizId: 1,
  fileIds: '1,2'
})

const fileList = ref([])

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canViewFile = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.FILE_VIEW))

const pageDesc = computed(() => {
  if (canViewFile.value) {
    return '查看附件记录并执行业务绑定。'
  }

  return '当前角色无附件模块访问权限。'
})

async function loadFiles() {
  if (!canViewFile.value) return

  loading.value = true
  try {
    const payload = await fileApi.list({ bizType: bindForm.bizType, bizId: bindForm.bizId })
    const data = payload?.data || {}
    fileList.value = Array.isArray(data) ? data : Array.isArray(data.children) ? data.children : []
  } catch (error) {
    fileList.value = []
    ElMessage.error(error?.message || '附件列表加载失败')
  } finally {
    loading.value = false
  }
}

async function bindFiles() {
  if (!canViewFile.value) {
    ElMessage.warning('当前角色没有附件绑定权限')
    return
  }

  try {
    const ids = bindForm.fileIds
      .split(',')
      .map((item) => Number(item.trim()))
      .filter(Boolean)

    await fileApi.bind({ bizType: bindForm.bizType, bizId: bindForm.bizId, fileIds: ids })
    ElMessage.success('附件绑定成功')
  } catch (error) {
    ElMessage.error(error?.message || '附件绑定失败')
  }
}

loadFiles()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">文件附件</h1>
        <p class="page-desc">{{ pageDesc }}</p>
      </div>
    </div>

    <el-empty v-if="!canViewFile" description="当前角色不可访问附件模块" />

    <div v-else class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>附件列表</span></template>
        <el-table :data="fileList" stripe v-loading="loading">
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="文件名" prop="fileName" min-width="180" />
          <el-table-column label="地址" prop="fileUrl" min-width="260" />
          <el-table-column label="大小" prop="fileSize" width="100" />
          <el-table-column label="类型" prop="contentType" min-width="140" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>绑定业务记录</span></template>
        <el-form label-position="top">
          <el-form-item label="业务类型">
            <el-input v-model="bindForm.bizType" />
          </el-form-item>
          <el-form-item label="业务 ID">
            <el-input-number v-model="bindForm.bizId" class="full-width" />
          </el-form-item>
          <el-form-item label="附件 IDs">
            <el-input v-model="bindForm.fileIds" placeholder="如 1,2,3" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="bindFiles">执行绑定</el-button>
      </el-card>
    </div>
  </div>
</template>
