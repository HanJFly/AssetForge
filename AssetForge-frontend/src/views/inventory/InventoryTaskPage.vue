<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { inventoryApi, departmentApi, categoryApi, userApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { ACTION_CODES, getRoleDataScope, roleHasAction } from '@/utils/role-access'
import { getCurrentUserProfile } from '@/utils/data-scope'

const loading = ref(false)
const detailLoading = ref(false)
const reportLoading = ref(false)
const submitting = ref(false)

const taskList = ref([])
const detailList = ref([])
const selectedTaskId = ref(null)
const selectedDetailId = ref(null)

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canCreateInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_CREATE))
const canExecuteInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_EXECUTE))
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const currentUser = computed(() => getCurrentUserProfile())
const isGlobalScope = computed(() => dataScope.value === 'GLOBAL')

const submitForm = reactive({
  taskId: null,
  detailList: [
    {
      detailId: null,
      result: 'NORMAL',
      actualUserId: null,
      actualLocation: '',
      remark: ''
    }
  ]
})

const reportForm = reactive({
  id: null
})

const conclusionForm = reactive({
  id: null,
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

const dialogVisible = ref(false)
const createForm = reactive({
  taskName: '',
  scopeType: 'ALL',
  scopeValue: [],
  assetStatusFilter: [],
  deadLine: '',
  responsibleUserId: null
})

const departmentOptions = ref([])
const categoryOptions = ref([])
const userOptions = ref([])

const assetStatusOptions = [
  { label: '在库', value: 'STOCK' },
  { label: '使用中', value: 'ASSIGNED' }
]

const pageDesc = computed(() => {
  if (canCreateInventory.value && canExecuteInventory.value) {
    return '查看盘点任务、提交盘点结果并维护盘点结论。'
  }
  if (canExecuteInventory.value) {
    return '查看盘点任务并提交盘点结果。'
  }
  return '查看盘点任务、盘点明细与盘点报告。'
})

const filteredDepartmentOptions = computed(() => {
  if (!departmentOptions.value.length) return []
  if (isGlobalScope.value) return departmentOptions.value
  return departmentOptions.value.filter((dept) => dept.id === currentUser.value.departmentId)
})

const isResponsibleLocked = computed(() => isGlobalScope.value && currentUser.value.userId)

function resetCreateForm() {
  createForm.taskName = ''
  createForm.scopeType = 'ALL'
  createForm.scopeValue = []
  createForm.assetStatusFilter = []
  createForm.deadLine = ''
  createForm.responsibleUserId = null
}

function resetSubmitForm() {
  submitForm.detailList = [
    {
      detailId: null,
      result: 'NORMAL',
      actualUserId: null,
      actualLocation: '',
      remark: ''
    }
  ]
  selectedDetailId.value = null
}

function fillSubmitFormFromDetail(detail) {
  if (!detail) {
    resetSubmitForm()
    return
  }
  submitForm.detailList = [
    {
      detailId: detail.id ?? null,
      result: detail.result || 'NORMAL',
      actualUserId: detail.actualUserId ?? detail.systemUserId ?? null,
      actualLocation: detail.actualLocation || '',
      remark: detail.remark || ''
    }
  ]
  selectedDetailId.value = detail.id ?? null
}

function taskRowClassName({ row }) {
  return row?.id === selectedTaskId.value ? 'is-selected-row' : ''
}

function detailRowClassName({ row }) {
  return row?.id === selectedDetailId.value ? 'is-selected-row' : ''
}

async function loadDepartmentOptions() {
  try {
    const payload = await departmentApi.getAll()
    departmentOptions.value = payload?.data ?? []
  } catch (_error) {
    departmentOptions.value = []
  }
}

async function loadCategoryOptions() {
  try {
    const payload = await categoryApi.page({ page: 1, size: 200 })
    categoryOptions.value = payload?.data?.records ?? payload?.data ?? []
  } catch (_error) {
    categoryOptions.value = []
  }
}

async function loadUserOptions() {
  try {
    const payload = await userApi.page({ page: 1, size: 200 })
    userOptions.value = payload?.data?.records ?? payload?.data ?? []
  } catch (_error) {
    userOptions.value = []
  }
}

async function openCreateDialog() {
  resetCreateForm()
  await Promise.all([loadDepartmentOptions(), loadCategoryOptions(), loadUserOptions()])
  if (isGlobalScope.value && currentUser.value.userId) {
    createForm.responsibleUserId = currentUser.value.userId
  }
  if (!isGlobalScope.value && currentUser.value.departmentId) {
    createForm.scopeValue = [currentUser.value.departmentId]
  }
  dialogVisible.value = true
}

async function submitCreateTask() {
  if (!createForm.taskName) {
    ElMessage.warning('请输入盘点任务名称')
    return
  }
  if (!createForm.deadLine) {
    ElMessage.warning('请选择截止日期')
    return
  }
  if (!createForm.responsibleUserId) {
    ElMessage.warning('请选择负责人')
    return
  }

  submitting.value = true
  try {
    await inventoryApi.taskCreate({
      taskName: createForm.taskName,
      scopeType: createForm.scopeType,
      scopeValue: createForm.scopeType === 'ALL' ? [] : createForm.scopeValue,
      assetStatusFilter: createForm.assetStatusFilter,
      deadLine: createForm.deadLine,
      responsibleUserId: Number(createForm.responsibleUserId)
    })
    ElMessage.success('盘点任务创建成功')
    dialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '盘点任务创建失败')
  } finally {
    submitting.value = false
  }
}

async function loadTaskDetails(taskId) {
  if (!taskId) {
    detailList.value = []
    resetSubmitForm()
    return
  }
  detailLoading.value = true
  try {
    const detailPayload = await inventoryApi.detailPage({
      page: 1,
      size: 50,
      taskId,
      result: null
    })
    detailList.value = normalizePageResult(detailPayload, []).records
    if (detailList.value.length > 0) {
      fillSubmitFormFromDetail(detailList.value[0])
    } else {
      resetSubmitForm()
    }
  } catch (error) {
    detailList.value = []
    resetSubmitForm()
    ElMessage.error(error?.message || '盘点明细加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleTaskSelect(row) {
  if (!row?.id) return
  selectedTaskId.value = row.id
  submitForm.taskId = row.id
  reportForm.id = row.id
  conclusionForm.id = row.id
  await loadTaskDetails(row.id)
}

function handleDetailSelect(row) {
  if (!row?.id) return
  fillSubmitFormFromDetail(row)
}

async function loadTasks() {
  loading.value = true
  try {
    const taskPayload = await inventoryApi.taskPage({ page: 1, size: 10, status: null, scopeType: null })
    taskList.value = normalizePageResult(taskPayload, []).records.map((item) => ({
      ...item,
      deadline: item.deadline || item.deadLine || ''
    }))

    const currentTask =
      taskList.value.find((item) => item.id === selectedTaskId.value) ||
      taskList.value[0]

    if (currentTask) {
      await handleTaskSelect(currentTask)
    } else {
      selectedTaskId.value = null
      submitForm.taskId = null
      reportForm.id = null
      conclusionForm.id = null
      detailList.value = []
      resetSubmitForm()
    }
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
  if (!submitForm.taskId) {
    ElMessage.warning('请先选择盘点任务')
    return
  }
  if (!submitForm.detailList[0]?.detailId) {
    ElMessage.warning('请先选择要提交的盘点明细')
    return
  }

  try {
    await inventoryApi.detailSubmit(submitForm)
    ElMessage.success('盘点结果已提交')
    await loadTaskDetails(submitForm.taskId)
  } catch (error) {
    ElMessage.error(error?.message || '盘点结果提交失败')
  }
}

async function loadReport() {
  if (!reportForm.id) {
    ElMessage.warning('请先选择盘点任务')
    return
  }
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
  if (!conclusionForm.id) {
    ElMessage.warning('请先选择盘点任务')
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

onMounted(async () => {
  await loadUserOptions()
  await loadTasks()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">盘点管理</h1>
        <p class="page-desc">{{ pageDesc }}</p>
      </div>
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header>
          <div class="card-header-actions">
            <span>盘点任务</span>
            <el-button v-if="canCreateInventory" type="primary" @click="openCreateDialog">创建盘点任务</el-button>
          </div>
        </template>
        <el-table
          :data="taskList"
          v-loading="loading"
          stripe
          :row-class-name="taskRowClassName"
          @row-click="handleTaskSelect"
        >
          <el-table-column label="任务编号" prop="id" width="100" />
          <el-table-column label="任务名称" prop="taskName" min-width="220" />
          <el-table-column label="范围类型" prop="scopeType" width="130" />
          <el-table-column label="截止日期" prop="deadline" width="120" />
          <el-table-column label="负责人编号" prop="responsibleUserId" width="120" />
          <el-table-column label="状态" prop="status" width="120" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>结果提交</span></template>
        <el-empty
          v-if="!canExecuteInventory"
          description="当前角色无盘点执行权限，仅保留任务与明细查看。"
        />
        <template v-else>
          <div class="form-tip">先在左侧选择盘点任务，再在下方选择要提交的盘点明细。</div>
          <el-form label-position="top">
            <el-form-item label="任务编号">
              <el-input :model-value="submitForm.taskId ?? ''" disabled />
            </el-form-item>
            <el-form-item label="明细编号">
              <el-input :model-value="submitForm.detailList[0]?.detailId ?? ''" disabled />
            </el-form-item>
            <el-form-item label="盘点结果">
              <el-select v-model="submitForm.detailList[0].result" class="full-width">
                <el-option label="正常" value="NORMAL" />
                <el-option label="盘亏" value="LOSS" />
                <el-option label="盘盈" value="GAIN" />
                <el-option label="账实不符" value="MISMATCH" />
              </el-select>
            </el-form-item>
            <el-form-item label="实际使用人编号">
              <el-input-number v-model="submitForm.detailList[0].actualUserId" class="full-width" />
            </el-form-item>
            <el-form-item label="实际位置">
              <el-input v-model="submitForm.detailList[0].actualLocation" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="submitForm.detailList[0].remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
          <div class="toolbar-row">
            <el-button type="primary" @click="submitResult">提交盘点结果</el-button>
          </div>
        </template>
      </el-card>
    </div>

    <el-card shadow="never" class="page-card">
      <template #header><span>盘点明细</span></template>
      <el-table
        :data="detailList"
        v-loading="detailLoading"
        stripe
        :row-class-name="detailRowClassName"
        @row-click="handleDetailSelect"
      >
        <el-table-column label="明细编号" prop="id" width="100" />
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
          <el-form-item label="任务编号">
            <el-input :model-value="reportForm.id ?? ''" disabled />
          </el-form-item>
        </el-form>
        <div class="toolbar-row">
          <el-button :loading="reportLoading" type="primary" @click="loadReport">查询报告</el-button>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务编号">{{ reportResult.taskId ?? '-' }}</el-descriptions-item>
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
        <template v-else>
          <el-form label-position="top">
            <el-form-item label="任务编号">
              <el-input :model-value="conclusionForm.id ?? ''" disabled />
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
          <div class="toolbar-row">
            <el-button type="primary" @click="submitConclusion">提交结论</el-button>
          </div>
        </template>
      </el-card>
    </div>
  </div>

  <el-dialog v-model="dialogVisible" title="创建盘点任务" width="560px" :close-on-click-modal="false">
    <el-form label-position="top">
      <el-form-item label="任务名称" required>
        <el-input v-model="createForm.taskName" placeholder="请输入盘点任务名称" />
      </el-form-item>
      <el-form-item label="范围类型" required>
        <el-select v-model="createForm.scopeType" class="full-width">
          <el-option label="全部资产" value="ALL" />
          <el-option label="按部门" value="DEPARTMENT" />
          <el-option label="按分类" value="CATEGORY" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="createForm.scopeType === 'DEPARTMENT'" label="选择部门">
        <el-select v-model="createForm.scopeValue" multiple class="full-width" placeholder="请选择部门，可多选">
          <el-option
            v-for="dept in filteredDepartmentOptions"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
        <div v-if="!isGlobalScope" class="form-tip">当前角色只能查看本部门的盘点任务</div>
      </el-form-item>
      <el-form-item v-if="createForm.scopeType === 'CATEGORY'" label="选择分类">
        <el-select v-model="createForm.scopeValue" multiple class="full-width" placeholder="请选择分类，可多选">
          <el-option
            v-for="cat in categoryOptions"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资产状态筛选">
        <el-select v-model="createForm.assetStatusFilter" multiple class="full-width" placeholder="不选则包含所有状态">
          <el-option
            v-for="opt in assetStatusOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="截止日期" required>
        <el-date-picker
          v-model="createForm.deadLine"
          type="date"
          class="full-width"
          placeholder="选择截止日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="负责人" required>
        <el-select
          v-model="createForm.responsibleUserId"
          class="full-width"
          placeholder="请选择负责人"
          :disabled="isResponsibleLocked"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.realName || user.username"
            :value="user.id"
          />
        </el-select>
        <div v-if="isResponsibleLocked" class="form-tip">
          自动设为当前登录用户：{{ currentUser.realName }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitCreateTask">确认创建</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.card-header-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.form-tip {
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

:deep(.is-selected-row) {
  background: rgba(64, 158, 255, 0.08) !important;
}
</style>
