<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { categoryApi, requisitionApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { normalizePageResult } from '@/api/helpers'
import { ACTION_CODES, getRoleDataScope, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const categoryOptions = ref([])

const baseFilters = [
  { label: '申领单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '申请人', prop: 'applicantName', placeholder: '按申请人模糊查询' },
  { label: '申请部门', prop: 'departmentName', placeholder: '按部门名称模糊查询' },
  { label: '审批人', prop: 'approverName', placeholder: '按审批人模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: '申领单号', prop: 'orderNo', minWidth: 160 },
  { label: '申请人', prop: 'applicantName', minWidth: 120 },
  { label: '审批状态', prop: 'approvalStatus', width: 120 },
  { label: '创建时间', prop: 'createdAt', minWidth: 170 }
]

const detailFields = [
  { label: '申领单号', prop: 'orderNo' },
  { label: '申请人', prop: 'applicantName' },
  { label: '申请原因', prop: 'reason' },
  { label: '审批状态', prop: 'approvalStatus' }
]

const detailItemColumns = [
  { label: '明细编号', prop: 'id', width: 100 },
  { label: '分类编号', prop: 'categoryId', width: 110 },
  { label: '分类名称', prop: 'categoryName', minWidth: 160 },
  { label: '数量', prop: 'quantity', width: 90 }
]

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const canCreateRequisition = computed(() =>
  roleHasAction(selectedRoleCode.value, ACTION_CODES.REQUISITION_CREATE)
)
const currentUser = computed(() => getCurrentUserProfile())

function normalizeName(value) {
  return String(value || '').trim().toLowerCase()
}

function buildCategorySuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return categoryOptions.value
    .filter((item) => {
      const isLeaf = item.parentId != null && item.parentId !== 0
      return isLeaf && (!keyword || normalizeName(item.name).includes(keyword))
    })
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function queryCategorySuggestions(queryString, callback) {
  callback(buildCategorySuggestions(queryString))
}

function handleCategorySelect(item, formModel) {
  formModel.categoryName = item.value
  formModel.categoryId = item.id
}

function syncCategoryByName(formModel) {
  const normalized = normalizeName(formModel.categoryName)
  if (!normalized) {
    formModel.categoryId = null
    return
  }

  const matched = categoryOptions.value.find((item) => {
    const isLeaf = item.parentId != null && item.parentId !== 0
    return isLeaf && normalizeName(item.name) === normalized
  })

  formModel.categoryId = matched?.id ?? null
}

async function loadCategories() {
  try {
    const payload = await categoryApi.page({ page: 1, size: 1000, name: '', parentId: null })
    categoryOptions.value = normalizePageResult(payload, []).records
  } catch (error) {
    categoryOptions.value = []
    ElMessage.error(error?.message || '分类列表加载失败')
  }
}

const filters = computed(() =>
  baseFilters.map((field) => ({
    ...field,
    hidden:
      dataScope.value === 'SELF'
        ? ['applicantName', 'departmentName'].includes(field.prop)
        : false
  }))
)

const formFields = computed(() => [
  {
    label: '当前申请人',
    prop: 'currentApplicantName',
    default: () => currentUser.value.realName || '当前登录用户',
    componentProps: { disabled: true }
  },
  {
    label: '当前申请部门',
    prop: 'currentDepartmentName',
    default: () => currentUser.value.departmentName || '当前所属部门',
    componentProps: { disabled: true }
  },
  { label: '申请原因', prop: 'reason', type: 'textarea', placeholder: '请输入申请原因' },
  { label: '期望日期', prop: 'expectedDate', type: 'date' },
  {
    label: '分类名称',
    prop: 'categoryName',
    type: 'autocomplete',
    placeholder: '输入分类名称并从下拉中选择',
    onSelect: handleCategorySelect,
    componentProps: {
      fetchSuggestions: queryCategorySuggestions,
      valueKey: 'value',
      triggerOnFocus: true,
      clearable: true
    }
  },
  { label: '分类编号', prop: 'categoryId', hidden: true },
  { label: '数量', prop: 'quantity', type: 'number', default: 1 }
])

function submitPayloadBuilder({ formModel }) {
  syncCategoryByName(formModel)

  if (!formModel.categoryId) {
    ElMessage.warning('请选择有效的分类名称')
    throw new Error('invalid category')
  }

  return {
    reason: formModel.reason,
    expectedDate: formModel.expectedDate,
    itemList: [
      {
        categoryId: Number(formModel.categoryId),
        quantity: Number(formModel.quantity || 1)
      }
    ]
  }
}

function queryPayloadBuilder({ queryModel, pagination }) {
  return buildScopedQuery(
    {
      ...queryModel,
      page: pagination.page,
      size: pagination.size
    },
    {
      scope: dataScope.value,
      self: () => ({
        applicantName: currentUser.value.realName,
        departmentName: currentUser.value.departmentName
      }),
      department: () => ({
        departmentName: currentUser.value.departmentName
      })
    }
  )
}

loadCategories()
</script>

<template>
  <ModuleScaffold
    title="资产申领"
    description="查询申领记录并发起资产申领。出库确认已独立迁移到出库作业页。"
    :filters="filters"
    :columns="columns"
    :form-fields="formFields"
    :detail-fields="detailFields"
    detail-table-title="申领物品明细"
    detail-table-prop="itemList"
    :detail-table-columns="detailItemColumns"
    :submit-payload-builder="submitPayloadBuilder"
    :query-payload-builder="queryPayloadBuilder"
    :permissions="{ create: canCreateRequisition }"
    :api="requisitionApi"
  />
</template>
