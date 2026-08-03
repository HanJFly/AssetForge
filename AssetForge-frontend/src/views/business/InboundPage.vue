<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { assetApi, returnApi } from '@/api'
import { normalizeDataResult, normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const loading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const detailModel = ref(null)
const detailVisible = ref(false)
const assetOptions = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const queryModel = reactive({
  orderNo: '',
  returnUserName: '',
  approvalStatus: '',
  startDate: '',
  endDate: ''
})

const confirmForm = reactive({
  id: null,
  orderNo: '',
  assetCode: '',
  inboundRemark: '已完成入库验收'
})

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canConfirmInbound = computed(() =>
  roleHasAction(selectedRoleCode.value, ACTION_CODES.RETURN_INBOUND)
)

function normalizeCode(value) {
  return String(value || '').trim().toUpperCase()
}

function findAssetByCode(assetCode) {
  const normalized = normalizeCode(assetCode)
  if (!normalized) return null

  return (
    assetOptions.value.find((item) => normalizeCode(item.assetCode) === normalized) || null
  )
}

async function resolveAssetByCode(assetCode) {
  const localMatched = findAssetByCode(assetCode)
  if (localMatched?.id) {
    return localMatched
  }

  const normalized = normalizeCode(assetCode)
  if (!normalized) {
    return null
  }

  try {
    const payload = await assetApi.page({
      page: 1,
      size: 20,
      assetCode: normalized,
      name: '',
      categoryName: '',
      departmentName: ''
    })

    const records = normalizePageResult(payload, []).records
    return records.find((item) => normalizeCode(item.assetCode) === normalized) || null
  } catch (_error) {
    return null
  }
}

async function loadAssets() {
  try {
    const payload = await assetApi.page({
      page: 1,
      size: 1000,
      assetCode: '',
      name: '',
      categoryName: '',
      departmentName: ''
    })
    assetOptions.value = normalizePageResult(payload, []).records
  } catch (error) {
    assetOptions.value = []
    ElMessage.error(error?.message || '资产列表加载失败')
  }
}

function buildQueryPayload() {
  return {
    ...queryModel,
    page: pagination.page,
    size: pagination.size
  }
}

async function loadList() {
  if (!canConfirmInbound.value) return

  loading.value = true
  try {
    const payload = await returnApi.page(buildQueryPayload())
    const result = normalizePageResult(payload, [])
    tableData.value = result.records
    pagination.total = result.total
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    ElMessage.error(error?.message || '入库单列表加载失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const payload = await returnApi.detail({ id: row.id })
    detailModel.value = normalizeDataResult(payload, row) || row
  } catch (error) {
    detailModel.value = row
    ElMessage.error(error?.message || '入库单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function fillConfirmForm(row) {
  try {
    const payload = await returnApi.detail({ id: row.id })
    const detail = normalizeDataResult(payload, row) || row
    const firstItem = Array.isArray(detail.itemList) ? detail.itemList[0] : null
    const matchedAsset =
      firstItem?.assetId != null
        ? assetOptions.value.find((item) => Number(item.id) === Number(firstItem.assetId))
        : null

    confirmForm.id = detail.id ?? row.id ?? null
    confirmForm.orderNo = detail.orderNo || row.orderNo || ''
    confirmForm.assetCode = firstItem?.assetCode || matchedAsset?.assetCode || ''

    ElMessage.success('已带入入库单信息')
  } catch (error) {
    ElMessage.error(error?.message || '带入入库单失败')
  }
}

async function submitInbound() {
  if (!confirmForm.id) {
    ElMessage.warning('请先选择需要处理的入库单')
    return
  }

  submitting.value = true
  try {
    await returnApi.confirmInbound({
      id: Number(confirmForm.id),
      confirmRemark: confirmForm.inboundRemark
    })
    ElMessage.success('入库确认已提交')
    loadList()
  } catch (error) {
    ElMessage.error(error?.message || '入库确认失败')
  } finally {
    submitting.value = false
  }
}

function resetQuery() {
  Object.assign(queryModel, {
    orderNo: '',
    returnUserName: '',
    approvalStatus: '',
    startDate: '',
    endDate: ''
  })
  pagination.page = 1
  loadList()
}

function handlePageChange(page) {
  pagination.page = page
  loadList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadList()
}

loadAssets()
loadList()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">入库作业</h1>
        <p class="page-desc">仓库管理员与资产管理员可集中处理归还入库确认。</p>
      </div>
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>待处理归还单</span></template>

        <el-form label-width="90px" inline>
          <el-form-item label="归还单号">
            <el-input v-model="queryModel.orderNo" placeholder="按单号前缀查询" clearable />
          </el-form-item>
          <el-form-item label="归还人">
            <el-input v-model="queryModel.returnUserName" placeholder="按归还人查询" clearable />
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="queryModel.approvalStatus" placeholder="请选择" clearable>
              <el-option
                v-for="option in approvalStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-row">
          <el-button type="primary" @click="pagination.page = 1; loadList()">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>

        <el-table :data="tableData" v-loading="loading" stripe style="margin-top: 16px">
          <el-table-column label="归还单号" prop="orderNo" min-width="160" />
          <el-table-column label="归还人" prop="returnUserName" min-width="120" />
          <el-table-column label="审批状态" prop="approvalStatus" width="120" />
          <el-table-column label="创建时间" prop="createdAt" min-width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="fillConfirmForm(row)">带入入库</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :current-page="pagination.page"
            :page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>入库确认</span></template>
        <el-form label-position="top">
          <el-form-item label="归还单号">
            <el-input v-model="confirmForm.orderNo" disabled />
          </el-form-item>
          <el-form-item label="单据编号">
            <el-input-number v-model="confirmForm.id" class="full-width" />
          </el-form-item>
          <el-form-item label="资产编码">
            <el-input
              v-model="confirmForm.assetCode"
              class="full-width"
              placeholder="请输入完整资产编码，例如 AST-20260701-000008"
            />
          </el-form-item>
          <el-form-item label="入库说明">
            <el-input v-model="confirmForm.inboundRemark" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>

        <div class="toolbar-row">
          <el-button type="primary" :loading="submitting" @click="submitInbound">提交入库确认</el-button>
        </div>
      </el-card>
    </div>

    <el-drawer v-model="detailVisible" title="归还单详情" size="540px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailModel" :column="1" border>
          <el-descriptions-item label="归还单号">{{ detailModel.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="归还人">{{ detailModel.returnUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="归还原因">{{ detailModel.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">{{ detailModel.approvalStatus || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">归还明细</div>
        <el-table :data="detailModel?.itemList || []" stripe>
          <el-table-column label="明细编号" prop="id" width="100" />
          <el-table-column label="资产编码" prop="assetCode" width="160" />
          <el-table-column label="资产内部编号" prop="assetId" width="120" />
          <el-table-column label="资产状态" prop="assetCondition" min-width="140" />
          <el-table-column label="状态说明" prop="conditionRemark" min-width="180" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.section-title {
  margin: 20px 0 12px;
  font-size: 16px;
  font-weight: 600;
}

.full-width {
  width: 100%;
}
</style>
