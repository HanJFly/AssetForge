<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { approvalApi } from '@/api'
import { normalizeDataResult, normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const activeTab = ref('todo')
const loading = ref(false)
const todoList = ref([])
const doneList = ref([])
const detail = ref(null)

const actionForm = reactive({
  id: null,
  decision: 'APPROVED',
  comment: '同意办理'
})

const transferForm = reactive({
  id: null,
  targetApproverId: 9,
  comment: '请协助处理'
})

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canReviewApproval = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.APPROVAL_REVIEW))
const currentUser = computed(() => getCurrentUserProfile())
const detailFormData = computed(() => (Array.isArray(detail.value?.formData) ? detail.value.formData : []))
const detailHistoryList = computed(() => (Array.isArray(detail.value?.historyList) ? detail.value.historyList : []))
const pageDesc = computed(() => (
  canReviewApproval.value
    ? '处理待审批事项并查看审批记录。'
    : '查看与本人相关的审批记录。'
))

function isNotFoundError(error) {
  return error?.code === 404 || error?.response?.status === 404
}

async function loadData() {
  loading.value = true
  try {
    const todoRequest = approvalApi.todoPage({ page: 1, size: 10, approvalType: null })
    const doneRequest = approvalApi.donePage({ page: 1, size: 10, approvalType: null, approvalStatus: null })

    const [todoResult, doneResult] = await Promise.allSettled([todoRequest, doneRequest])

    if (todoResult.status === 'fulfilled') {
      todoList.value = normalizePageResult(todoResult.value, []).records
    } else {
      todoList.value = []
      if (!isNotFoundError(todoResult.reason)) {
        ElMessage.error(todoResult.reason?.msg || todoResult.reason?.message || '待审批数据加载失败')
      }
    }

    if (doneResult.status === 'fulfilled') {
      doneList.value = normalizePageResult(doneResult.value, []).records
    } else {
      doneList.value = []
      if (!isNotFoundError(doneResult.reason)) {
        ElMessage.error(doneResult.reason?.msg || doneResult.reason?.message || '已审批数据加载失败')
      }
    }
  } catch (error) {
    todoList.value = []
    doneList.value = []
    ElMessage.error(error?.msg || error?.message || '审批数据加载失败')
  } finally {
    loading.value = false
  }
}

async function loadDetail(row) {
  try {
    const payload = await approvalApi.detail({ id: row.id })
    detail.value = normalizeDataResult(payload, row) || row
    transferForm.id = row.id
  } catch (error) {
    detail.value = row
    transferForm.id = row.id
    ElMessage.error(error?.msg || error?.message || '审批详情加载失败')
  }
}

async function submitAction(row) {
  actionForm.id = row.id
  try {
    await approvalApi.action({ ...actionForm })
    ElMessage.success('审批已提交')
    await loadData()
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '审批提交失败')
  }
}

async function submitTransfer() {
  try {
    await approvalApi.transfer({ ...transferForm })
    ElMessage.success('转交审批已提交')
    await loadData()
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '转交审批失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">审批中心</h1>
        <p class="page-desc">{{ pageDesc }}</p>
      </div>
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="待我审批" name="todo">
            <el-table :data="todoList" v-loading="loading" stripe>
              <el-table-column label="流程号" prop="processNo" min-width="150" />
              <el-table-column label="审批类型" prop="approvalType" width="120" />
              <el-table-column label="标题" prop="title" min-width="220" />
              <el-table-column label="申请人" prop="applicantName" width="120" />
              <el-table-column label="状态" prop="status" width="100" />
              <el-table-column label="创建时间" prop="createdAt" min-width="170" />
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="loadDetail(row)">详情</el-button>
                  <el-button v-if="canReviewApproval" link type="primary" @click="submitAction(row)">审批</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="已审批" name="done">
            <el-table :data="doneList" v-loading="loading" stripe>
              <el-table-column label="流程号" prop="processNo" min-width="150" />
              <el-table-column label="业务类型" prop="businessType" width="120" />
              <el-table-column label="标题" prop="title" min-width="220" />
              <el-table-column label="结果" prop="decision" width="100" />
              <el-table-column label="审批时间" prop="approvedAt" min-width="170" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="primary" @click="loadDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>审批详情</span></template>
        <el-empty v-if="!detail" description="请选择审批记录查看详情" />
        <template v-else>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="流程号">{{ detail.processNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ detail.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审批类型">{{ detail.approvalType || detail.businessType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ detail.status || detail.approvalStatus || detail.decision || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前审批人">{{ detail.currentApproverName || '-' }}</el-descriptions-item>
          </el-descriptions>

          <template v-if="canReviewApproval">
            <el-divider>审批操作</el-divider>
            <el-form label-width="90px">
              <el-form-item label="审批结果">
                <el-select v-model="actionForm.decision" class="full-width">
                  <el-option label="通过" value="APPROVED" />
                  <el-option label="驳回" value="REJECTED" />
                </el-select>
              </el-form-item>
              <el-form-item label="审批意见">
                <el-input v-model="actionForm.comment" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="toolbar-row">
              <el-button type="primary" @click="submitAction(detail)">提交审批</el-button>
            </div>

            <el-divider>转交审批</el-divider>
            <el-form label-width="90px">
              <el-form-item label="目标审批人 ID">
                <el-input-number v-model="transferForm.targetApproverId" class="full-width" />
              </el-form-item>
              <el-form-item label="转交说明">
                <el-input v-model="transferForm.comment" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="toolbar-row">
              <el-button @click="submitTransfer">提交转交</el-button>
            </div>
          </template>

          <el-divider>表单数据</el-divider>
          <el-empty v-if="detailFormData.length === 0" description="暂无表单数据" />
          <template v-else>
            <el-card
              v-for="(form, index) in detailFormData"
              :key="`${form.orderNo || 'form'}-${index}`"
              shadow="never"
              class="detail-subcard"
            >
              <el-descriptions :column="1" border>
                <el-descriptions-item label="单号">{{ form.orderNo || '-' }}</el-descriptions-item>
                <el-descriptions-item label="原因">{{ form.reason || '-' }}</el-descriptions-item>
              </el-descriptions>
              <el-table v-if="Array.isArray(form.itemList) && form.itemList.length > 0" :data="form.itemList" stripe>
                <el-table-column label="分类 ID" prop="categoryId" width="100" />
                <el-table-column label="分类名称" prop="categoryName" min-width="180" />
                <el-table-column label="数量" prop="quantity" width="100" />
              </el-table>
            </el-card>
          </template>

          <el-divider>历史记录</el-divider>
          <el-empty v-if="detailHistoryList.length === 0" description="暂无审批历史" />
          <el-table v-else :data="detailHistoryList" stripe>
            <el-table-column label="审批人" prop="approverName" width="120" />
            <el-table-column label="结果" prop="decision" width="120" />
            <el-table-column label="意见" prop="comment" min-width="220" />
            <el-table-column label="时间" prop="actionTime" min-width="170" />
          </el-table>
        </template>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.detail-subcard {
  margin-bottom: 12px;
}
</style>
