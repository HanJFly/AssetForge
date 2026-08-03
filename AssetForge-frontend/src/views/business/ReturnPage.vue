<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { assetApi, returnApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { normalizePageResult } from '@/api/helpers'
import { ACTION_CODES, getRoleDataScope, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const conditionOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '轻微损坏', value: 'MINOR_DAMAGE' },
  { label: '严重损坏', value: 'MAJOR_DAMAGE' }
]

const assetOptions = ref([])

const baseFilters = [
  { label: '归还单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '归还人', prop: 'returnUserName', placeholder: '按归还人模糊查询' },
  { label: '工号', prop: 'returnUserEmployeeNo', placeholder: '按工号模糊查询' },
  { label: '归还部门', prop: 'returnUserDepartmentName', placeholder: '按部门名称模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: '归还单号', prop: 'orderNo', minWidth: 160 },
  { label: '归还人', prop: 'returnUserName', minWidth: 120 },
  { label: '审批状态', prop: 'approvalStatus', width: 120 },
  { label: '创建时间', prop: 'createdAt', minWidth: 170 }
]

const detailFields = [
  { label: '归还单号', prop: 'orderNo' },
  { label: '归还人', prop: 'returnUserName' },
  { label: '归还原因', prop: 'reason' },
  { label: '审批状态', prop: 'approvalStatus' }
]

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const canCreateReturn = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.RETURN_CREATE))
const currentUser = computed(() => getCurrentUserProfile())

function normalizeName(value) {
  return String(value || '').trim().toLowerCase()
}

function buildAssetSuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return assetOptions.value
    .filter((item) => {
      const name = normalizeName(item.name)
      const code = normalizeName(item.assetCode)
      return !keyword || name.includes(keyword) || code.includes(keyword)
    })
    .map((item) => ({
      value: item.name || item.assetCode || '',
      id: item.id,
      assetCode: item.assetCode || ''
    }))
}

function queryAssetSuggestions(queryString, callback) {
  callback(buildAssetSuggestions(queryString))
}

function handleAssetSelect(item, formModel) {
  formModel.assetName = item.value
  formModel.assetId = item.id
}

function findAssetByName(name) {
  const normalized = normalizeName(name)
  if (!normalized) return null
  return assetOptions.value.find((item) => normalizeName(item.name || item.assetCode) === normalized) || null
}

async function loadAssets() {
  try {
    const payload = await assetApi.page({
      page: 1,
      size: 1000,
      name: '',
      assetCode: '',
      categoryName: '',
      departmentName: ''
    })
    assetOptions.value = normalizePageResult(payload, []).records
  } catch (error) {
    assetOptions.value = []
    ElMessage.error(error?.message || '资产列表加载失败')
  }
}

const filters = computed(() =>
  baseFilters.map((field) => ({
    ...field,
    hidden:
      dataScope.value === 'SELF'
        ? ['returnUserName', 'returnUserEmployeeNo', 'returnUserDepartmentName'].includes(field.prop)
        : false
  }))
)

const formFields = computed(() => [
  {
    label: '当前归还人',
    prop: 'currentReturnUserName',
    default: () => currentUser.value.realName || '当前登录用户',
    componentProps: { disabled: true }
  },
  {
    label: '当前工号',
    prop: 'currentReturnEmployeeNo',
    default: () => currentUser.value.employeeNo || '未配置工号',
    componentProps: { disabled: true }
  },
  {
    label: '当前归还部门',
    prop: 'currentReturnDepartmentName',
    default: () => currentUser.value.departmentName || '当前所属部门',
    componentProps: { disabled: true }
  },
  { label: '归还原因', prop: 'reason', type: 'textarea', placeholder: '请输入归还原因' },
  { label: '预计归还日期', prop: 'expectedReturnDate', type: 'date' },
  {
    label: '资产名称',
    prop: 'assetName',
    type: 'autocomplete',
    placeholder: '输入资产名称并选择',
    onSelect: handleAssetSelect,
    componentProps: {
      fetchSuggestions: queryAssetSuggestions,
      triggerOnFocus: true,
      valueKey: 'value',
      clearable: true
    }
  },
  { label: '资产内部编号', prop: 'assetId', hidden: true },
  { label: '归还状态', prop: 'assetCondition', type: 'select', options: conditionOptions },
  { label: '状态说明', prop: 'conditionRemark', type: 'textarea', placeholder: '请输入状态说明' }
])

function submitPayloadBuilder({ formModel }) {
  formModel.assetId = findAssetByName(formModel.assetName)?.id ?? null

  if (!formModel.assetId) {
    ElMessage.warning('请选择有效的资产名称')
    throw new Error('invalid asset')
  }

  return {
    reason: formModel.reason,
    expectedReturnDate: formModel.expectedReturnDate,
    itemList: [
      {
        assetId: Number(formModel.assetId),
        assetCondition: formModel.assetCondition,
        conditionRemark: formModel.conditionRemark
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
        returnUserName: currentUser.value.realName,
        returnUserEmployeeNo: currentUser.value.employeeNo,
        returnUserDepartmentName: currentUser.value.departmentName
      }),
      department: () => ({
        returnUserDepartmentName: currentUser.value.departmentName
      })
    }
  )
}

loadAssets()
</script>

<template>
  <ModuleScaffold
    title="资产归还"
    description="查询归还记录并发起归还申请，表单已改为按资产名称选择。入库确认已独立迁移到入库作业页。"
    :filters="filters"
    :columns="columns"
    :form-fields="formFields"
    :detail-fields="detailFields"
    :submit-payload-builder="submitPayloadBuilder"
    :query-payload-builder="queryPayloadBuilder"
    :permissions="{ create: canCreateReturn }"
    :api="returnApi"
  />
</template>
