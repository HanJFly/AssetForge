<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import { inventoryApi, departmentApi, categoryApi, userApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { formatInventoryResult, formatInventoryTaskStatus, formatScopeType } from '@/utils/display-map'
import { ACTION_CODES, getRoleDataScope, roleHasAction } from '@/utils/role-access'
import { getCurrentUserProfile } from '@/utils/data-scope'

const loading = ref(false)
const detailLoading = ref(false)
const reportLoading = ref(false)
const createSubmitting = ref(false)
const resultSubmitting = ref(false)

const taskList = ref([])
const detailList = ref([])
const selectedTaskId = ref(null)
const selectedDetailRows = ref([])

const createDialogVisible = ref(false)
const resultDialogVisible = ref(false)

const dialogMode = ref('single')
const resultTarget = ref(null)

const departmentOptions = ref([])
const categoryOptions = ref([])
const userOptions = ref([])

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canCreateInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_CREATE))
const canExecuteInventory = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.INVENTORY_EXECUTE))
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const currentUser = computed(() => getCurrentUserProfile())
const isGlobalScope = computed(() => dataScope.value === 'GLOBAL')

const selectedTask = computed(() => taskList.value.find((item) => item.id === selectedTaskId.value) || null)
const isTaskCompleted = computed(() => selectedTask.value?.status === 'COMPLETED')
const selectedCount = computed(() => selectedDetailRows.value.length)

const pageDesc = computed(() => {
  if (canCreateInventory.value && canExecuteInventory.value) {
    return '选择盘点任务后，可批量处理盘点明细；盘盈场景继续单条处理。'
  }
  if (canExecuteInventory.value) {
    return '选择盘点任务后，可批量提交盘点明细结果。'
  }
  return '查看盘点任务、盘点明细、盘点报告与盘点结论。'
})

const filteredDepartmentOptions = computed(() => {
  if (!departmentOptions.value.length) return []
  if (isGlobalScope.value) return departmentOptions.value
  return departmentOptions.value.filter((dept) => dept.id === currentUser.value.departmentId)
})

const isResponsibleLocked = computed(() => isGlobalScope.value && currentUser.value.userId)

const assetStatusOptions = [
  { label: '在库', value: 'STOCK' },
  { label: '使用中', value: 'ASSIGNED' }
]

const resultOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '盘亏', value: 'LOSS' },
  { label: '盘盈', value: 'GAIN' },
  { label: '账实不符', value: 'MISMATCH' }
]

const batchResultOptions = computed(() => resultOptions.filter((item) => item.value !== 'GAIN'))

const createForm = reactive({
  taskName: '',
  scopeType: 'ALL',
  scopeValue: [],
  assetStatusFilter: [],
  deadLine: '',
  responsibleUserId: null
})

const detailPagination = reactive({
  page: 1,
  size: 10,
  total: 0
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

const conclusionForm = reactive({
  conclusion: '',
  status: 'COMPLETED'
})

const resultForm = reactive({
  detailId: null,
  result: 'NORMAL',
  actualUserId: null,
  actualLocation: '',
  remark: '',
  foundAssetName: '',
  foundAssetCategory: '',
  foundAssetLocation: '',
  foundAssetCode: ''
})

watch(
  () => resultForm.result,
  (nextResult) => {
    if (nextResult !== 'GAIN') {
      resultForm.foundAssetName = ''
      resultForm.foundAssetCategory = ''
      resultForm.foundAssetLocation = ''
      resultForm.foundAssetCode = ''
    }

    if (nextResult === 'LOSS') {
      resultForm.actualUserId = null
      resultForm.actualLocation = ''
    }
  }
)

function formatResult(value) {
  return formatInventoryResult(value)
}

function formatTaskStatus(value) {
  return formatInventoryTaskStatus(value)
}

function getTaskStatusType(value) {
  if (value === 'COMPLETED') return 'info'
  if (value === 'IN_PROGRESS') return 'success'
  return 'warning'
}

function resetCreateForm() {
  createForm.taskName = ''
  createForm.scopeType = 'ALL'
  createForm.scopeValue = []
  createForm.assetStatusFilter = []
  createForm.deadLine = ''
  createForm.responsibleUserId = null
}

function resetResultForm() {
  resultForm.detailId = null
  resultForm.result = 'NORMAL'
  resultForm.actualUserId = null
  resultForm.actualLocation = ''
  resultForm.remark = ''
  resultForm.foundAssetName = ''
  resultForm.foundAssetCategory = ''
  resultForm.foundAssetLocation = ''
  resultForm.foundAssetCode = ''
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
  createDialogVisible.value = true
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

  createSubmitting.value = true
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
    createDialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '盘点任务创建失败')
  } finally {
    createSubmitting.value = false
  }
}

async function loadReport() {
  if (!selectedTaskId.value) {
    reportResult.value = {
      taskId: null,
      taskName: '',
      totalCount: 0,
      normalCount: 0,
      lossCount: 0,
      gainCount: 0,
      mismatchCount: 0,
      lossRate: 0
    }
    return
  }

  reportLoading.value = true
  try {
    const payload = await inventoryApi.report({ id: Number(selectedTaskId.value) })
    reportResult.value = payload?.data || reportResult.value
  } catch (error) {
    ElMessage.error(error?.message || '盘点报告查询失败')
  } finally {
    reportLoading.value = false
  }
}

function syncTaskDerivedState() {
  conclusionForm.conclusion = selectedTask.value?.conclusion || ''
  conclusionForm.status = 'COMPLETED'
}

function decorateDetailRows(records) {
  return records.map((row) => ({
    ...row,
    resultLabel: formatResult(row.result)
  }))
}

function clearDetailSelection() {
  selectedDetailRows.value = []
}

async function loadTaskDetails(taskId) {
  if (!taskId) {
    detailList.value = []
    detailPagination.total = 0
    clearDetailSelection()
    return
  }

  detailLoading.value = true
  try {
    const payload = await inventoryApi.detailPage({
      page: detailPagination.page,
      size: detailPagination.size,
      taskId,
      result: null
    })
    const pageResult = normalizePageResult(payload, [])
    detailList.value = decorateDetailRows(pageResult.records)
    detailPagination.total = pageResult.total
    detailPagination.page = pageResult.page
    detailPagination.size = pageResult.size
    clearDetailSelection()
  } catch (error) {
    detailList.value = []
    detailPagination.total = 0
    clearDetailSelection()
    ElMessage.error(error?.message || '盘点明细加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleTaskSelect(row) {
  if (!row?.id) return
  selectedTaskId.value = row.id
  detailPagination.page = 1
  syncTaskDerivedState()
  await Promise.all([loadTaskDetails(row.id), loadReport()])
}

function taskRowClassName({ row }) {
  return row?.id === selectedTaskId.value ? 'is-selected-row' : ''
}

function handleDetailSelectionChange(selection) {
  selectedDetailRows.value = selection
}

function openSingleResultDialog(row) {
  if (!row?.id) return
  if (isTaskCompleted.value) {
    ElMessage.warning('当前盘点任务已完成，不能继续提交盘点结果')
    return
  }

  dialogMode.value = 'single'
  resultTarget.value = row
  resetResultForm()
  resultForm.detailId = row.id
  resultForm.result = row.result || 'NORMAL'
  resultForm.actualUserId = row.actualUserId ?? row.systemUserId ?? null
  resultDialogVisible.value = true
}

function openBatchResultDialog() {
  if (isTaskCompleted.value) {
    ElMessage.warning('当前盘点任务已完成，不能继续提交盘点结果')
    return
  }
  if (selectedDetailRows.value.length === 0) {
    ElMessage.warning('请先勾选要批量处理的盘点明细')
    return
  }

  dialogMode.value = 'batch'
  resultTarget.value = null
  resetResultForm()
  resultDialogVisible.value = true
}

function buildSinglePayload() {
  const payload = {
    detailId: resultForm.detailId,
    result: resultForm.result,
    actualUserId: resultForm.actualUserId,
    actualLocation: resultForm.actualLocation?.trim() || null,
    remark: resultForm.remark?.trim() || null,
    foundAssetName: null,
    foundAssetCategory: null,
    foundAssetLocation: null,
    foundAssetCode: null
  }

  if (resultForm.result === 'LOSS') {
    payload.actualUserId = null
    payload.actualLocation = null
  }

  if (resultForm.result === 'GAIN') {
    payload.foundAssetName = resultForm.foundAssetName?.trim() || null
    payload.foundAssetCategory = resultForm.foundAssetCategory?.trim() || null
    payload.foundAssetLocation = resultForm.foundAssetLocation?.trim() || null
    payload.foundAssetCode = resultForm.foundAssetCode?.trim() || null
  }

  return payload
}

function buildBatchPayload() {
  return selectedDetailRows.value.map((row) => ({
    detailId: row.id,
    result: resultForm.result,
    actualUserId:
      resultForm.result === 'LOSS'
        ? null
        : resultForm.actualUserId ?? row.actualUserId ?? row.systemUserId ?? null,
    actualLocation:
      resultForm.result === 'LOSS'
        ? null
        : resultForm.actualLocation?.trim() || null,
    remark: resultForm.remark?.trim() || null,
    foundAssetName: null,
    foundAssetCategory: null,
    foundAssetLocation: null,
    foundAssetCode: null
  }))
}

function validateSingleResultForm() {
  if (!selectedTaskId.value) {
    ElMessage.warning('请先选择盘点任务')
    return false
  }
  if (!resultForm.detailId) {
    ElMessage.warning('请选择要提交的盘点明细')
    return false
  }
  if ((resultForm.result === 'NORMAL' || resultForm.result === 'MISMATCH') && !resultForm.actualUserId) {
    ElMessage.warning('请选择实际使用人')
    return false
  }
  if (resultForm.result === 'GAIN') {
    if (!resultForm.foundAssetName?.trim()) {
      ElMessage.warning('请填写盘盈资产名称')
      return false
    }
    if (!resultForm.foundAssetCategory?.trim()) {
      ElMessage.warning('请填写盘盈资产分类')
      return false
    }
    if (!resultForm.foundAssetLocation?.trim()) {
      ElMessage.warning('请填写盘盈资产存放地点')
      return false
    }
    if (!resultForm.foundAssetCode?.trim()) {
      ElMessage.warning('请填写盘盈资产条码')
      return false
    }
  }
  return true
}

function validateBatchResultForm() {
  if (!selectedTaskId.value) {
    ElMessage.warning('请先选择盘点任务')
    return false
  }
  if (selectedDetailRows.value.length === 0) {
    ElMessage.warning('请先勾选要批量处理的盘点明细')
    return false
  }
  if ((resultForm.result === 'NORMAL' || resultForm.result === 'MISMATCH') && !resultForm.actualUserId) {
    ElMessage.warning('批量处理正常/账实不符时，请先选择实际使用人')
    return false
  }
  if (resultForm.result === 'GAIN') {
    ElMessage.warning('盘盈场景请使用单条处理，不支持批量提交')
    return false
  }
  return true
}

async function submitResult() {
  if (!canExecuteInventory.value) {
    ElMessage.warning('当前角色没有提交盘点结果的权限')
    return
  }

  const isBatch = dialogMode.value === 'batch'
  const valid = isBatch ? validateBatchResultForm() : validateSingleResultForm()
  if (!valid) {
    return
  }

  resultSubmitting.value = true
  try {
    await inventoryApi.detailSubmit({
      taskId: selectedTaskId.value,
      detailList: isBatch ? buildBatchPayload() : [buildSinglePayload()]
    })
    ElMessage.success(isBatch ? `批量提交成功，共处理 ${selectedDetailRows.value.length} 条明细` : '盘点明细结果提交成功')
    resultDialogVisible.value = false
    await Promise.all([loadTaskDetails(selectedTaskId.value), loadReport()])
  } catch (error) {
    ElMessage.error(error?.message || '盘点明细结果提交失败')
  } finally {
    resultSubmitting.value = false
  }
}

async function submitConclusion() {
  if (!canCreateInventory.value) {
    ElMessage.warning('当前角色没有更新盘点结论的权限')
    return
  }
  if (!selectedTaskId.value) {
    ElMessage.warning('请先选择盘点任务')
    return
  }

  try {
    await inventoryApi.conclusion({
      id: Number(selectedTaskId.value),
      conclusion: conclusionForm.conclusion,
      status: 'COMPLETED'
    })
    ElMessage.success('盘点结论提交成功')
    await loadTasks()
  } catch (error) {
    ElMessage.error(error?.message || '盘点结论提交失败')
  }
}

async function handleDetailPageChange(page) {
  detailPagination.page = page
  await loadTaskDetails(selectedTaskId.value)
}

async function handleDetailSizeChange(size) {
  detailPagination.size = size
  detailPagination.page = 1
  await loadTaskDetails(selectedTaskId.value)
}

async function loadTasks() {
  loading.value = true
  try {
    const payload = await inventoryApi.taskPage({
      page: 1,
      size: 10,
      status: null,
      scopeType: null
    })
    taskList.value = normalizePageResult(payload, []).records.map((item) => ({
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
      detailList.value = []
      detailPagination.total = 0
      clearDetailSelection()
      syncTaskDerivedState()
      await loadReport()
    }
  } catch (error) {
    taskList.value = []
    detailList.value = []
    detailPagination.total = 0
    clearDetailSelection()
    ElMessage.error(error?.message || '盘点数据加载失败')
  } finally {
    loading.value = false
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
        <el-table-column label="任务名称" prop="taskName" min-width="240" />
        <el-table-column label="范围类型" width="140">
          <template #default="{ row }">{{ formatScopeType(row.scopeType) }}</template>
        </el-table-column>
        <el-table-column label="截止日期" prop="deadline" width="140" />
        <el-table-column label="负责人编号" prop="responsibleUserId" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)">{{ formatTaskStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="split-grid compact-gap">
      <el-card shadow="never" class="page-card">
        <template #header><span>任务概览</span></template>

        <el-empty v-if="!selectedTask" description="请选择盘点任务" />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="任务名称">{{ selectedTask.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务编号">{{ selectedTask.id }}</el-descriptions-item>
          <el-descriptions-item label="范围类型">{{ formatScopeType(selectedTask.scopeType) }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ selectedTask.deadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ formatTaskStatus(selectedTask.status) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>明细处理</span></template>

        <el-empty
          v-if="!canExecuteInventory"
          description="当前角色无盘点执行权限，仅保留查看能力。"
        />

        <template v-else>
          <div class="form-tip">先选择盘点任务，再在明细表勾选多条记录进行批量处理。</div>
          <div class="form-tip">批量处理支持：正常、盘亏、账实不符。</div>
          <div class="form-tip">盘盈需要录入每条资产的发现信息，仍保留单条处理。</div>
          <div class="toolbar-row">
            <el-button type="primary" :disabled="selectedCount === 0 || isTaskCompleted" @click="openBatchResultDialog">
              批量处理所选明细
            </el-button>
            <span class="subtle-text">已勾选 {{ selectedCount }} 条</span>
          </div>
          <div v-if="isTaskCompleted" class="form-tip warning-tip">当前任务已完成，不能继续提交盘点结果。</div>
        </template>
      </el-card>
    </div>

    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header-actions">
          <span>盘点明细</span>
          <span class="subtle-text">{{ selectedTask ? `当前任务：${selectedTask.taskName}` : '请先选择盘点任务' }}</span>
        </div>
      </template>

      <el-table :data="detailList" v-loading="detailLoading" stripe @selection-change="handleDetailSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="明细编号" prop="id" width="100" />
        <el-table-column label="资产编码" prop="assetCode" min-width="160" />
        <el-table-column label="资产名称" prop="assetName" min-width="180" />
        <el-table-column label="系统使用人" prop="systemUserName" width="140" />
        <el-table-column label="当前结果" width="140">
          <template #default="{ row }">
            {{ formatResult(row.result) }}
          </template>
        </el-table-column>
        <el-table-column label="实际使用人" prop="actualUserName" width="140" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!canExecuteInventory || isTaskCompleted" @click="openSingleResultDialog(row)">
              单条处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="detailPagination.total"
          :current-page="detailPagination.page"
          :page-size="detailPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handleDetailPageChange"
          @size-change="handleDetailSizeChange"
        />
      </div>
    </el-card>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>盘点报告</span></template>

        <el-empty v-if="!selectedTask" description="请选择盘点任务后查看报告" />
        <template v-else>
          <div class="toolbar-row">
            <el-button :loading="reportLoading" type="primary" @click="loadReport">刷新报告</el-button>
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
        </template>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>盘点结论</span></template>

        <el-empty
          v-if="!canCreateInventory"
          description="当前角色无盘点任务管理权限，仅保留报告查看。"
        />

        <template v-else>
          <el-form label-position="top">
            <el-form-item label="当前任务">
              <el-input :model-value="selectedTask?.taskName || ''" disabled />
            </el-form-item>
            <el-form-item label="结论">
              <el-input v-model="conclusionForm.conclusion" type="textarea" :rows="5" placeholder="填写本次盘点结论后提交结案" />
            </el-form-item>
            <el-form-item label="提交状态">
              <el-input :model-value="formatTaskStatus('COMPLETED')" disabled />
            </el-form-item>
          </el-form>

          <div class="toolbar-row">
            <el-button type="primary" @click="submitConclusion" :disabled="!selectedTask">提交结论并结案</el-button>
          </div>
        </template>
      </el-card>
    </div>
  </div>

  <el-dialog v-model="createDialogVisible" title="创建盘点任务" width="560px" :close-on-click-modal="false">
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
        <div v-if="!isGlobalScope" class="form-tip">当前角色只能查看本部门的盘点任务。</div>
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
        <div v-if="isResponsibleLocked" class="form-tip">已自动设为当前登录用户：{{ currentUser.realName }}</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="createSubmitting" @click="submitCreateTask">确认创建</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="resultDialogVisible"
    :title="dialogMode === 'batch' ? '批量处理盘点明细' : '提交盘点明细结果'"
    width="640px"
    :close-on-click-modal="false"
  >
    <div v-if="dialogMode === 'batch'" class="form-tip">
      本次将批量处理 {{ selectedCount }} 条明细。批量模式不支持“盘盈”，如需盘盈请使用单条处理。
    </div>

    <el-form label-position="top">
      <template v-if="dialogMode === 'single'">
        <el-form-item label="明细编号">
          <el-input :model-value="resultTarget?.id || ''" disabled />
        </el-form-item>

        <el-form-item label="资产编码">
          <el-input :model-value="resultTarget?.assetCode || ''" disabled />
        </el-form-item>

        <el-form-item label="资产名称">
          <el-input :model-value="resultTarget?.assetName || ''" disabled />
        </el-form-item>

        <el-form-item label="系统使用人">
          <el-input :model-value="resultTarget?.systemUserName || ''" disabled />
        </el-form-item>
      </template>

      <el-form-item label="盘点结果" required>
        <el-select v-model="resultForm.result" class="full-width">
          <el-option
            v-for="opt in dialogMode === 'batch' ? batchResultOptions : resultOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template v-if="resultForm.result === 'NORMAL' || resultForm.result === 'MISMATCH'">
        <el-form-item label="实际使用人" required>
          <el-select
            v-model="resultForm.actualUserId"
            filterable
            clearable
            class="full-width"
            placeholder="请选择实际使用人"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="实际位置">
          <el-input v-model="resultForm.actualLocation" placeholder="可选，填写资产当前存放位置" />
        </el-form-item>
      </template>

      <template v-if="dialogMode === 'single' && resultForm.result === 'GAIN'">
        <el-form-item label="实际使用人">
          <el-select
            v-model="resultForm.actualUserId"
            filterable
            clearable
            class="full-width"
            placeholder="可选，填写盘盈资产当前使用人"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="实际位置">
          <el-input v-model="resultForm.actualLocation" placeholder="填写盘盈资产当前存放位置" />
        </el-form-item>

        <el-divider>盘盈资产信息</el-divider>

        <el-form-item label="盘盈资产名称" required>
          <el-input v-model="resultForm.foundAssetName" placeholder="请输入盘盈资产名称" />
        </el-form-item>

        <el-form-item label="盘盈资产分类" required>
          <el-input v-model="resultForm.foundAssetCategory" placeholder="请输入盘盈资产分类" />
        </el-form-item>

        <el-form-item label="盘盈资产存放地点" required>
          <el-input v-model="resultForm.foundAssetLocation" placeholder="请输入盘盈资产存放地点" />
        </el-form-item>

        <el-form-item label="盘盈资产条码" required>
          <el-input v-model="resultForm.foundAssetCode" placeholder="请输入盘盈资产条码" />
        </el-form-item>
      </template>

      <template v-if="resultForm.result === 'LOSS'">
        <div class="form-tip warning-tip">盘亏提交后，系统会清空实际使用人与实际位置，并在任务结案时生成盘亏处理数据。</div>
      </template>

      <el-form-item label="备注">
        <el-input v-model="resultForm.remark" type="textarea" :rows="3" placeholder="可选，填写盘点备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="resultDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="resultSubmitting" @click="submitResult">
        {{ dialogMode === 'batch' ? '批量提交结果' : '提交结果' }}
      </el-button>
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

.compact-gap {
  margin-top: 20px;
}

.form-tip {
  margin-bottom: 12px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

.warning-tip {
  color: #d97706;
}

.subtle-text {
  font-size: 12px;
  color: #94a3b8;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

:deep(.is-selected-row) {
  background: rgba(64, 158, 255, 0.08) !important;
}
</style>
