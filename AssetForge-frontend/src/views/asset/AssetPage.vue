<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { assetApi, categoryApi, departmentApi } from '@/api'
import http from '@/api/http'
import { normalizeDataResult, normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { formatAssetStatus, formatSourceType } from '@/utils/display-map'
import { ACTION_CODES, DATA_SCOPES, getRoleDataScope, ROLE_CODES, roleHasAction } from '@/utils/role-access'

const assetStatusOptions = [
  { label: '库存', value: 'STOCK' },
  { label: '已领用', value: 'ASSIGNED' },
  { label: '已报废', value: 'SCRAPPED' },
  { label: '盘亏', value: 'LOST' }
]

const sourceTypeOptions = [
  { label: '采购', value: 'PURCHASE' },
  { label: '租赁', value: 'LEASE' }
]

const categoryLookup = ref([])
const departmentLookup = ref([])
const ledgerLoading = ref(false)

const leafCategoryLookup = computed(() => {
  const parentIds = new Set(
    categoryLookup.value
      .map((item) => item?.parentId)
      .filter((parentId) => parentId != null && parentId !== '' && Number(parentId) !== 0)
      .map((parentId) => Number(parentId))
  )

  return categoryLookup.value.filter((item) => item?.id != null && !parentIds.has(Number(item.id)))
})

function resolveBackendFileUrl(fileUrl) {
  if (!fileUrl) {
    return ''
  }

  if (/^https?:\/\//i.test(fileUrl) || fileUrl.startsWith('/')) {
    return fileUrl
  }

  return `/api/image/${fileUrl}`
}

function mapAttachmentListToFileList(attachmentList = []) {
  return attachmentList.map((attachment) => ({
    name: attachment.fileName || attachment.fileUrl || `附件-${attachment.id}`,
    url: resolveBackendFileUrl(attachment.fileUrl),
    status: 'success',
    response: attachment,
    uid: attachment.id,
    id: attachment.id
  }))
}

const assetFormApi = {
  ...assetApi,
  detail: async (data) => {
    const payload = await assetApi.detail(data)
    const detail = normalizeDataResult(payload, {}) || {}

    return {
      ...payload,
      data: {
        ...detail,
        attachmentFiles: mapAttachmentListToFileList(detail.attachmentList || [])
      }
    }
  },
  create: (data) => assetApi.createWithFiles(data)
}

const baseFilters = [
  { label: '资产编码', prop: 'assetCode', placeholder: '按编码前缀查询' },
  { label: '资产名称', prop: 'name', placeholder: '按名称模糊查询' },
  { label: '品牌型号', prop: 'brandModel', placeholder: '按品牌型号模糊查询' },
  { label: '使用人', prop: 'realName', placeholder: '按当前使用人模糊查询' },
  { label: '资产状态', prop: 'assetStatus', type: 'select', options: assetStatusOptions },
  { label: '来源类型', prop: 'sourceType', type: 'select', options: sourceTypeOptions },
  { label: '分类名称', prop: 'categoryName', placeholder: '按分类名称模糊查询' },
  { label: '部门名称', prop: 'departmentName', placeholder: '按部门名称模糊查询' },
  { label: '采购开始', prop: 'purchaseDateStart', type: 'date' },
  { label: '采购结束', prop: 'purchaseDateEnd', type: 'date' }
]

const columns = [
  { label: '资产编码', prop: 'assetCode', minWidth: 160 },
  { label: '资产名称', prop: 'name', minWidth: 180 },
  { label: '分类', prop: 'categoryName', minWidth: 150 },
  { label: '部门', prop: 'departmentName', minWidth: 140 },
  { label: '当前使用人', prop: 'currentUserName', minWidth: 120 },
  { label: '状态', prop: 'status', width: 100, formatter: (_row, _column, value) => formatAssetStatus(value) },
  { label: '来源', prop: 'sourceType', width: 100, formatter: (_row, _column, value) => formatSourceType(value) },
  { label: '采购金额', prop: 'purchaseAmount', width: 120 },
  { label: '采购日期', prop: 'purchaseDate', width: 120 }
]

const baseFormFields = [
  { label: 'ID', prop: 'id', hidden: true },
  { label: '分类 ID', prop: 'categoryId', hidden: true },
  { label: '部门 ID', prop: 'departmentId', hidden: true },
  { label: '资产名称', prop: 'name', placeholder: '请输入资产名称' },
  {
    label: '分类名称',
    prop: 'categoryName',
    placeholder: '输入分类名称并从下拉提示中选择',
    type: 'autocomplete',
    componentProps: {
      fetchSuggestions: queryCategorySuggestions,
      triggerOnFocus: true,
      valueKey: 'value',
      clearable: true
    }
  },
  {
    label: '部门名称',
    prop: 'departmentName',
    placeholder: '输入部门名称并从下拉提示中选择',
    type: 'autocomplete',
    componentProps: {
      fetchSuggestions: queryDepartmentSuggestions,
      triggerOnFocus: true,
      valueKey: 'value',
      clearable: true
    }
  },
  { label: '存放地点', prop: 'location', placeholder: '可为空' },
  { label: '品牌型号', prop: 'brandModel', placeholder: '请输入品牌型号' },
  { label: '规格参数', prop: 'specification', type: 'textarea', placeholder: '可为空' },
  { label: '来源', prop: 'sourceType', type: 'select', options: sourceTypeOptions },
  { label: '用途', prop: 'purpose', placeholder: '如 OFFICE / RD / ADMIN' },
  { label: '采购金额', prop: 'purchaseAmount', type: 'number', default: 0 },
  { label: '采购日期', prop: 'purchaseDate', type: 'date' },
  { label: '供应商', prop: 'supplier', placeholder: '可为空' },
  {
    label: '附件上传',
    prop: 'attachmentFiles',
    type: 'upload',
    default: () => [],
    uploadButtonText: '上传附件',
    uploadTip: '请选择附件，保存时会与资产登记信息一并提交。',
    componentProps: {
      multiple: true,
      limit: 9,
      accept: '.png,.jpg,.jpeg,.pdf,.doc,.docx,.xls,.xlsx',
      autoUpload: false,
      listType: 'text',
      onPreview: previewAssetAttachment,
      beforeRemove: confirmRemoveAttachment,
      onRemove: () => ElMessage.success('已从附件列表移除'),
      onExceed: () => ElMessage.warning('最多上传 9 个附件')
    }
  },
  { label: '备注', prop: 'remark', type: 'textarea', placeholder: '请输入备注' }
]

const detailFields = [
  { label: '资产编码', prop: 'assetCode' },
  { label: '资产名称', prop: 'name' },
  { label: '分类', prop: 'categoryName' },
  { label: '部门', prop: 'departmentName' },
  { label: '当前使用人', prop: 'currentUserName' },
  { label: '品牌型号', prop: 'brandModel' },
  { label: '用途', prop: 'purpose' },
  { label: '状态', prop: 'status', formatter: (_row, _field, value) => formatAssetStatus(value) },
  { label: '来源', prop: 'sourceType', formatter: (_row, _field, value) => formatSourceType(value) },
  { label: '采购金额', prop: 'purchaseAmount' },
  { label: '采购日期', prop: 'purchaseDate' }
]

const ledgerFilters = reactive({
  snapshotMonth: '2026-07'
})

const ledgerPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const ledgerList = ref([])

const barcodeForm = reactive({
  id: 1
})

const barcodeDetail = ref(null)

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const isDeptManager = computed(() => selectedRoleCode.value === ROLE_CODES.DEPT_MANAGER)
const canManageAsset = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.ASSET_REGISTER))
const canViewLedger = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.ASSET_LEDGER_VIEW))
const currentUser = computed(() => getCurrentUserProfile())

const filters = computed(() => {
  if (dataScope.value === DATA_SCOPES.SELF) {
    return baseFilters.map((field) => ({
      ...field,
      hidden: field.prop === 'realName'
    }))
  }

  if (dataScope.value === DATA_SCOPES.DEPARTMENT_TREE) {
    return baseFilters.map((field) => ({
      ...field,
      hidden: field.prop === 'departmentName'
    }))
  }

  return baseFilters
})

const formFields = computed(() => baseFormFields.map((field) => {
  if (field.prop !== 'departmentName' || !isDeptManager.value) {
    return field
  }

  return {
    ...field,
    default: currentUser.value.departmentName || '',
    componentProps: {
      ...(field.componentProps || {}),
      disabled: true
    }
  }
}))

const assetNote = computed(() => {
  if (dataScope.value === DATA_SCOPES.SELF) {
    return '当前角色仅可查看本人名下资产，已隐藏新增、编辑、删除和台账入口。'
  }

  if (dataScope.value === DATA_SCOPES.DEPARTMENT_TREE) {
    return '当前角色仅可查看本部门资产，登记资产时部门将自动锁定为本人所属部门。'
  }

  return '当前角色可维护资产资料并查看资产台账。'
})

function normalizeName(value) {
  return String(value || '').trim().toLowerCase()
}

function buildSuggestions(source, queryString) {
  const keyword = normalizeName(queryString)

  return source
    .filter((item) => !keyword || normalizeName(item.name).includes(keyword))
    .sort((left, right) => {
      const leftName = normalizeName(left.name)
      const rightName = normalizeName(right.name)
      const leftStartsWith = keyword ? leftName.startsWith(keyword) : false
      const rightStartsWith = keyword ? rightName.startsWith(keyword) : false

      if (leftStartsWith !== rightStartsWith) {
        return leftStartsWith ? -1 : 1
      }

      return leftName.localeCompare(rightName, 'zh-CN')
    })
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function queryCategorySuggestions(queryString, callback) {
  callback(buildSuggestions(leafCategoryLookup.value, queryString))
}

function queryDepartmentSuggestions(queryString, callback) {
  callback(buildSuggestions(departmentLookup.value, queryString))
}

function resolveLookupId(source, name, label) {
  const normalized = normalizeName(name)
  const matched = source.find((item) => normalizeName(item.name) === normalized)

  if (!matched) {
    throw new Error(`请选择已有${label}`)
  }

  return matched.id
}

function findCategoryById(categoryId) {
  return categoryLookup.value.find((item) => Number(item.id) === Number(categoryId)) || null
}

function ensureLeafCategory(categoryId) {
  const category = findCategoryById(categoryId)

  if (!category) {
    throw new Error('请选择已有分类')
  }

  const hasChildren = categoryLookup.value.some((item) => Number(item.parentId) === Number(categoryId))
  if (hasChildren) {
    throw new Error('资产登记只能选择末级分类，请重新选择具体分类')
  }

  return category
}

function resolveAttachmentPreviewUrl(file) {
  if (file?.url) {
    return file.url
  }

  if (file?.raw instanceof File) {
    return URL.createObjectURL(file.raw)
  }

  return ''
}

async function confirmRemoveAttachment(file) {
  try {
    await ElMessageBox.confirm(`确认移除附件“${file.name}”吗？`, '移除附件', {
      type: 'warning'
    })
    return true
  } catch (_error) {
    return false
  }
}

function previewAssetAttachment(file) {
  const previewUrl = resolveAttachmentPreviewUrl(file)

  if (!previewUrl) {
    ElMessage.info('当前附件暂无可预览地址，请重新上传后查看')
    return
  }

  if (file?.raw instanceof File) {
    window.open(previewUrl, '_blank', 'noopener,noreferrer')
    return
  }

  http
    .get(previewUrl.replace(/^\/api/, ''), {
      responseType: 'blob'
    })
    .then((blob) => {
      const objectUrl = URL.createObjectURL(blob)
      window.open(objectUrl, '_blank', 'noopener,noreferrer')
      setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000)
    })
    .catch((error) => {
      ElMessage.warning(error?.message || '附件预览失败，请稍后重试')
    })
}

async function loadLookupData() {
  try {
    const [categoryPayload, departmentPayload] = await Promise.all([
      categoryApi.page({
        page: 1,
        size: 1000,
        pageSize: 1000
      }),
      departmentApi.getAll({})
    ])

    const categoryData = normalizePageResult(categoryPayload, []).records
    const departmentData = Array.isArray(departmentPayload?.data) ? departmentPayload.data : []

    categoryLookup.value = categoryData
      .filter((item) => item?.id != null && item?.name)
      .map((item) => ({ id: item.id, name: item.name, parentId: item.parentId ?? null }))
    departmentLookup.value = departmentData
      .filter((item) => item?.id != null && item?.name)
      .map((item) => ({ id: item.id, name: item.name }))
  } catch (_error) {
    ElMessage.warning('分类或部门数据加载失败，自动补全暂不可用')
  }
}

function submitPayloadBuilder({ mode, formModel }) {
  if (!Array.isArray(formModel.attachmentFiles) || formModel.attachmentFiles.length === 0) {
    throw new Error('请先选择附件后再保存')
  }

  const categoryId = resolveLookupId(categoryLookup.value, formModel.categoryName, '分类')
  ensureLeafCategory(categoryId)
  const departmentId = isDeptManager.value
    ? currentUser.value.departmentId
    : resolveLookupId(departmentLookup.value, formModel.departmentName, '部门')
  const departmentName = isDeptManager.value
    ? currentUser.value.departmentName
    : formModel.departmentName || ''

  if (isDeptManager.value && !departmentId) {
    throw new Error('当前账号未绑定所属部门，无法登记资产')
  }

  const attachmentIds = formModel.attachmentFiles
    .map((file) => file?.id ?? file?.response?.id ?? null)
    .filter((id) => id != null)

  if (mode === 'edit') {
    const hasNewLocalFiles = formModel.attachmentFiles.some((file) => file?.raw && !file?.id && !file?.response?.id)
    if (hasNewLocalFiles) {
      throw new Error('当前修改接口暂不支持直接新增附件，请保留已有附件后再保存')
    }

    return {
      id: formModel.id,
      name: formModel.name || '',
      categoryId,
      departmentId,
      departmentName,
      location: formModel.location || '',
      brandModel: formModel.brandModel || '',
      specification: formModel.specification || '',
      sourceType: formModel.sourceType || '',
      purpose: formModel.purpose || '',
      purchaseAmount: formModel.purchaseAmount ?? '',
      purchaseDate: formModel.purchaseDate || '',
      supplier: formModel.supplier || '',
      remark: formModel.remark || '',
      attachmentIds
    }
  }

  const formData = new FormData()
  formData.append('name', formModel.name || '')
  formData.append('categoryId', String(categoryId))
  formData.append('departmentId', String(departmentId))
  formData.append('location', formModel.location || '')
  formData.append('brandModel', formModel.brandModel || '')
  formData.append('specification', formModel.specification || '')
  formData.append('sourceType', formModel.sourceType || '')
  formData.append('purpose', formModel.purpose || '')
  formData.append('purchaseAmount', String(formModel.purchaseAmount ?? ''))
  formData.append('purchaseDate', formModel.purchaseDate || '')
  formData.append('supplier', formModel.supplier || '')
  formData.append('remark', formModel.remark || '')

  formModel.attachmentFiles.forEach((file) => {
    if (file.raw) {
      formData.append('files', file.raw)
    }
  })

  return formData
}

function queryPayloadBuilder({ queryModel, pagination }) {
  return buildScopedQuery(
    {
      ...queryModel,
      page: pagination.page,
      size: pagination.size,
      pageSize: pagination.size
    },
    {
      scope: dataScope.value,
      self: () => ({
        currentUserId: currentUser.value.userId
      }),
      department: () => ({
        departmentId: currentUser.value.departmentId,
        departmentName: currentUser.value.departmentName
      })
    }
  )
}

async function loadLedger() {
  ledgerLoading.value = true
  try {
    const payload = await assetApi.ledgerPage({
      page: ledgerPagination.page,
      pageSize: ledgerPagination.size,
      snapshotMonth: ledgerFilters.snapshotMonth || ''
    })

    const pageResult = normalizePageResult(payload, [])
    ledgerList.value = pageResult.records.map((item) => ({
      ...item,
      originalAmount: item.originalAmount ?? item.originalValue ?? 0,
      netAmount: item.netAmount ?? item.netValue ?? 0
    }))
    ledgerPagination.total = pageResult.total
    ledgerPagination.page = pageResult.page || ledgerPagination.page
    ledgerPagination.size = pageResult.size || ledgerPagination.size

    if (!ledgerList.value.length) {
      ElMessage.info('当前月份暂无台账数据')
    }
  } catch (error) {
    ElMessage.warning(error?.message || '资产台账查询失败')
  } finally {
    ledgerLoading.value = false
  }
}

function onLedgerSearch() {
  ledgerPagination.page = 1
  loadLedger()
}

function onLedgerPageChange(page) {
  ledgerPagination.page = page
  loadLedger()
}

function onLedgerPageSizeChange(size) {
  ledgerPagination.size = size
  ledgerPagination.page = 1
  loadLedger()
}

async function loadBarcode() {
  try {
    const payload = await assetApi.barcodeDetail({ id: barcodeForm.id })
    barcodeDetail.value = normalizeDataResult(payload, null)
  } catch (error) {
    barcodeDetail.value = null
    ElMessage.warning(error?.message || '条码查询失败')
  }
}

onMounted(loadLookupData)
</script>

<template>
  <div class="page-container">
    <ModuleScaffold
      title="资产管理"
      description="维护资产信息并执行分页查询、详情查看与登记操作。"
      :note="assetNote"
      :filters="filters"
      :columns="columns"
      :form-fields="formFields"
      :detail-fields="detailFields"
      :submit-payload-builder="submitPayloadBuilder"
      :query-payload-builder="queryPayloadBuilder"
      :permissions="{ create: canManageAsset, edit: canManageAsset, remove: canManageAsset }"
      :api="assetFormApi"
    />

    <div class="split-grid">
      <el-card v-if="canViewLedger" shadow="never" class="page-card">
        <template #header><span>资产台账分页</span></template>
        <el-form inline label-width="100px">
          <el-form-item label="快照月份">
            <el-date-picker
              v-model="ledgerFilters.snapshotMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
            />
          </el-form-item>
        </el-form>
        <div class="toolbar-row">
          <el-button type="primary" @click="onLedgerSearch">查询台账</el-button>
        </div>
        <el-table :data="ledgerList" stripe v-loading="ledgerLoading">
          <el-table-column label="资产编码" prop="assetCode" min-width="150" />
          <el-table-column label="资产名称" prop="assetName" min-width="160" />
          <el-table-column label="月份" prop="snapshotMonth" width="100" />
          <el-table-column label="原值" prop="originalAmount" width="100" />
          <el-table-column label="月折旧" prop="monthlyDepreciation" width="110" />
          <el-table-column label="累计折旧" prop="accumulatedDepreciation" width="110" />
          <el-table-column label="净值" prop="netAmount" width="100" />
        </el-table>
        <div style="display: flex; justify-content: flex-end; margin-top: 16px">
          <el-pagination
            v-model:current-page="ledgerPagination.page"
            v-model:page-size="ledgerPagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="ledgerPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="onLedgerPageChange"
            @size-change="onLedgerPageSizeChange"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>资产条码详情</span></template>
        <el-form label-width="90px">
          <el-form-item label="资产内部编号">
            <el-input-number v-model="barcodeForm.id" class="full-width" />
          </el-form-item>
        </el-form>
        <div class="toolbar-row">
          <el-button type="primary" @click="loadBarcode">查询条码</el-button>
        </div>
        <el-empty v-if="!barcodeDetail" description="输入资产内部编号后查询条码详情" />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="资产编码">{{ barcodeDetail.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="条码值">{{ barcodeDetail.barcodeValue }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ barcodeDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ barcodeDetail.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ barcodeDetail.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="当前使用人">{{ barcodeDetail.currentUserName }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>

    <el-card v-if="!canViewLedger" shadow="never" class="page-card">
      <template #header><span>资产台账分页</span></template>
      <el-empty description="当前角色无资产台账查看权限" />
    </el-card>
  </div>
</template>
