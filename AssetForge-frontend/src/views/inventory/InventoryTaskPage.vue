<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { inventoryApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const loading = ref(false)
const reportLoading = ref(false)

const taskList = ref([])
const detailList = ref([])

const submitForm = reactive({
  taskId: 1,
  detailList: [
    {
      detailId: 1,
      result: 'NORMAL',
      actualUserId: 1,
      remark: ''
    }
  ]
})

const reportForm = reactive({
  id: 1
})

const conclusionForm = reactive({
  id: 1,
  conclusion: '',
  status: 'COMPLETED'
})

const reportResult = ref({
  taskId: null,
  taskName: '',
  totalCount: 0,
  normalCount: 0,
  lossCount: 0,
  gainCount: 0,
  mismatchCount: 0,
  lossRate: 0
})

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canCreateInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_CREATE))
const canExecuteInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_EXECUTE))

const pageDesc = computed(() => {
  if (canCreateInventory.value && canExecuteInventory.value) {
    return '查看盘点任务、提交盘点结果并维护盘点结论。'
  }

  if (canExecuteInventory.value) {
    return '查看盘点任务并提交盘点结果。'
  }

  return '查看盘点任务、明细与盘点报告。'
})

async function loadTasks() {
  loading.value = true
  try {
    const [taskPayload, detailPayload] = await Promise.all([
      inventoryApi.taskPage({ page: 1, size: 10, status: null, scopeType: null }),
      inventoryApi.detailPage({ page: 1, size: 20, taskId: reportForm.id, result: null })
    ])

    taskList.value = normalizePageResult(taskPayload, []).records.map((item) => ({
      ...item,
      deadline: item.deadline || item.deadLine || ''
    }))
    detailList.value = normalizePageResult(detailPayload, []).records
  } catch (error) {
    taskList.value = []
    detailList.value = []
    ElMessage.error(error?.message || '盘点数据加载失败')
  } finally {
    loading.value = false
  }
}

async function submitResult() {
  if (!canExecuteInventory.value) {
    ElMessage.warning('当前角色没有提交盘点结果的权限')
    return
  }

  try {
    await inventoryApi.detailSubmit(submitForm)
    ElMessage.success('盘点结果已提交')
  } catch (error) {
    ElMessage.error(error?.message || '盘点结果提交失败')
  }
}

async function loadReport() {
  reportLoading.value = true
  try {
    const payload = await inventoryApi.report({ id: Number(reportForm.id) })
    reportResult.value = payload?.data || reportResult.value
    ElMessage.success('盘点报告已刷新')
  } catch (error) {
    ElMessage.error(error?.message || '盘点报告查询失败')
  } finally {
    reportLoading.value = false
  }
}

async function submitConclusion() {
  if (!canCreateInventory.value) {
    ElMessage.warning('当前角色没有更新盘点结论的权限')
    return
  }

  try {
    await inventoryApi.conclusion({
      id: Number(conclusionForm.id),
      conclusion: conclusionForm.conclusion,
      status: conclusionForm.status
    })
    ElMessage.success('盘点结论已提交')
  } catch (error) {
    ElMessage.error(error?.message || '盘点结论提交失败')
  }
}

loadTasks()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">盘点管理</h1>
        <p class="page-desc">{{ pageDesc }}</p>
      </div>
      <el-button v-if="canExecuteInventory" type="primary" @click="submitResult">提交盘点结果</el-button>
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>盘点任务</span></template>
        <el-table :data="taskList" v-loading="loading" stripe>
          <el-table-column label="任务 ID" prop="id" width="90" />
          <el-table-column label="任务名称" prop="taskName" min-width="220" />
          <el-table-column label="范围类型" prop="scopeType" width="130" />
          <el-table-column label="截止日期" prop="deadline" width="120" />
          <el-table-column label="负责人 ID" prop="responsibleUserId" width="120" />
          <el-table-column label="状态" prop="status" width="120" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>{{ canExecuteInventory ? '结果提交' : '结果提交' }}</span></template>
        <el-empty
          v-if="!canExecuteInventory"
          description="当前角色无盘点执行权限，仅保留任务与明细查看。"
        />
        <el-form v-else label-position="top">
          <el-form-item label="任务 ID">
            <el-input-number v-model="submitForm.taskId" class="full-width" />
          </el-form-item>
          <el-form-item label="盘点结果">
            <el-select v-model="submitForm.detailList[0].result" class="full-width">
              <el-option label="正常" value="NORMAL" />
              <el-option label="盘亏" value="LOSS" />
              <el-option label="盘盈" value="GAIN" />
              <el-option label="账实不符" value="MISMATCH" />
            </el-select>
          </el-form-item>
          <el-form-item label="实际使用人 ID">
            <el-input-number v-model="submitForm.detailList[0].actualUserId" class="full-width" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="submitForm.detailList[0].remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="never" class="page-card">
      <template #header><span>盘点明细</span></template>
      <el-table :data="detailList" v-loading="loading" stripe>
        <el-table-column label="明细 ID" prop="id" width="90" />
        <el-table-column label="资产编码" prop="assetCode" min-width="150" />
        <el-table-column label="资产名称" prop="assetName" min-width="180" />
        <el-table-column label="系统使用人" prop="systemUserName" width="120" />
        <el-table-column label="实际使用人" prop="actualUserName" width="120" />
        <el-table-column label="盘点结果" prop="result" width="120" />
      </el-table>
    </el-card>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>盘点报告</span></template>
        <el-form label-position="top">
          <el-form-item label="任务 ID">
            <el-input-number v-model="reportForm.id" class="full-width" />
          </el-form-item>
        </el-form>
        <div class="toolbar-row">
          <el-button :loading="reportLoading" type="primary" @click="loadReport">查询报告</el-button>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务 ID">{{ reportResult.taskId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ reportResult.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总数">{{ reportResult.totalCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="正常">{{ reportResult.normalCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="盘亏">{{ reportResult.lossCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="盘盈">{{ reportResult.gainCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="账实不符">{{ reportResult.mismatchCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="盘亏率">{{ reportResult.lossRate ?? 0 }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>更新结论</span></template>
        <el-empty
          v-if="!canCreateInventory"
          description="当前角色无盘点任务管理权限，仅保留报告查看。"
        />
        <el-form v-else label-position="top">
          <el-form-item label="任务 ID">
            <el-input-number v-model="conclusionForm.id" class="full-width" />
          </el-form-item>
          <el-form-item label="结论">
            <el-input v-model="conclusionForm.conclusion" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="conclusionForm.status" class="full-width">
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="进行中" value="IN_PROGRESS" />
            </el-select>
          </el-form-item>
        </el-form>
        <div v-if="canCreateInventory" class="toolbar-row">
          <el-button type="primary" @click="submitConclusion">提交结论</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>
