<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { assetApi, departmentApi, transferApi, userApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, DATA_SCOPES, getRoleDataScope, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const departmentOptions = ref([])
const userOptions = ref([])
const assetOptions = ref([])

const baseFilters = [
  { label: '调拨单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '调出人', prop: 'fromUserName', placeholder: '按调出人模糊查询' },
  { label: '调出部门', prop: 'fromUserDepartmentName', placeholder: '按调出部门模糊查询' },
  { label: '调入人', prop: 'toUserName', placeholder: '按调入人模糊查询' },
  { label: '调入部门', prop: 'toUserDepartmentName', placeholder: '按调入部门模糊查询' },
  { label: '审批人', prop: 'approverName', placeholder: '按审批人模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: '调拨单号', prop: 'orderNo', minWidth: 160 },
  { label: '调出部门', prop: 'fromUserDepartmentName', minWidth: 140 },
  { label: '调入部门', prop: 'toUserDepartmentName', minWidth: 140 },
  { label: '审批状态', prop: 'approvalStatus', width: 120 },
  { label: '创建时间', prop: 'createdAt', minWidth: 170 }
]

const detailFields = [
  { label: '调拨单号', prop: 'orderNo' },
  { label: '调出部门', prop: 'fromDepartmentName' },
  { label: '调入部门', prop: 'toDepartmentName' },
  { label: '调出人', prop: 'fromUserName' },
  { label: '调入人', prop: 'toUserName' },
  { label: '审批状态', prop: 'approvalStatus' }
]

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const canCreateTransfer = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.TRANSFER_CREATE))
const currentUser = computed(() => getCurrentUserProfile())

function normalizeName(value) {
  return String(value || '').trim().toLowerCase()
}

function buildDepartmentSuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return departmentOptions.value
    .filter((item) => !keyword || normalizeName(item.name).includes(keyword))
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function queryDepartmentSuggestions(queryString, callback) {
  callback(buildDepartmentSuggestions(queryString))
}

function buildUserSuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return userOptions.value
    .filter((item) => {
      const realName = normalizeName(item.realName)
      const username = normalizeName(item.username)
      return !keyword || realName.includes(keyword) || username.includes(keyword)
    })
    .map((item) => ({
      value: item.realName || item.username || '',
      id: item.id
    }))
}

function queryUserSuggestions(queryString, callback) {
  callback(buildUserSuggestions(queryString))
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

function findDepartmentByName(name) {
  const normalized = normalizeName(name)
  if (!normalized) return null
  return departmentOptions.value.find((item) => normalizeName(item.name) === normalized) || null
}

function findUserByName(name) {
  const normalized = normalizeName(name)
  if (!normalized) return null
  return userOptions.value.find((item) => normalizeName(item.realName || item.username) === normalized) || null
}

function findAssetByName(name) {
  const normalized = normalizeName(name)
  if (!normalized) return null
  return assetOptions.value.find((item) => normalizeName(item.name || item.assetCode) === normalized) || null
}

function handleDepartmentSelect(item, formModel, idProp, nameProp) {
  formModel[nameProp] = item.value
  formModel[idProp] = item.id
}

function handleUserSelect(item, formModel, idProp, nameProp) {
  formModel[nameProp] = item.value
  formModel[idProp] = item.id
}

function handleAssetSelect(item, formModel) {
  formModel.assetName = item.value
  formModel.assetId = item.id
}

async function loadLookups() {
  try {
    const [departmentPayload, userPayload, assetPayload] = await Promise.all([
      departmentApi.getAll({}),
      userApi.page({
        page: 1,
        size: 1000,
        username: '',
        realName: '',
        departmentName: '',
        status: ''
      }),
      assetApi.page({
        page: 1,
        size: 1000,
        name: '',
        assetCode: '',
        categoryName: '',
        departmentName: ''
      })
    ])

    departmentOptions.value = Array.isArray(departmentPayload?.data) ? departmentPayload.data : []
    userOptions.value = normalizePageResult(userPayload, []).records
    assetOptions.value = normalizePageResult(assetPayload, []).records
  } catch (error) {
    ElMessage.error(error?.message || '调拨基础数据加载失败')
  }
}

const filters = computed(() => {
  if (dataScope.value === DATA_SCOPES.GLOBAL) {
    return baseFilters
  }

  return baseFilters.map((field) => ({
    ...field,
    hidden: ['fromUserName', 'fromUserDepartmentName'].includes(field.prop)
  }))
})

const formFields = computed(() => {
  const lockFromSide = dataScope.value !== DATA_SCOPES.GLOBAL

  return [
    {
      label: '当前调出人',
      prop: 'currentFromUserName',
      default: () => currentUser.value.realName || '当前登录用户',
      componentProps: { disabled: true },
      hidden: !lockFromSide
    },
    {
      label: '当前调出部门',
      prop: 'currentFromDepartmentName',
      default: () => currentUser.value.departmentName || '当前所属部门',
      componentProps: { disabled: true },
      hidden: !lockFromSide
    },
    {
      label: '调出部门名称',
      prop: 'fromDepartmentName',
      type: 'autocomplete',
      placeholder: '输入调出部门名称并选择',
      default: () => (lockFromSide ? currentUser.value.departmentName || '' : ''),
      hidden: lockFromSide,
      onSelect: (item, formModel) =>
        handleDepartmentSelect(item, formModel, 'fromDepartmentId', 'fromDepartmentName'),
      componentProps: {
        fetchSuggestions: queryDepartmentSuggestions,
        triggerOnFocus: true,
        valueKey: 'value',
        clearable: true
      }
    },
    {
      label: '调入部门名称',
      prop: 'toDepartmentName',
      type: 'autocomplete',
      placeholder: '输入调入部门名称并选择',
      onSelect: (item, formModel) =>
        handleDepartmentSelect(item, formModel, 'toDepartmentId', 'toDepartmentName'),
      componentProps: {
        fetchSuggestions: queryDepartmentSuggestions,
        triggerOnFocus: true,
        valueKey: 'value',
        clearable: true
      }
    },
    {
      label: '调出人名称',
      prop: 'fromUserName',
      type: 'autocomplete',
      placeholder: '输入调出人名称并选择',
      default: () => (lockFromSide ? currentUser.value.realName || '' : ''),
      hidden: lockFromSide,
      onSelect: (item, formModel) => handleUserSelect(item, formModel, 'fromUserId', 'fromUserName'),
      componentProps: {
        fetchSuggestions: queryUserSuggestions,
        triggerOnFocus: true,
        valueKey: 'value',
        clearable: true
      }
    },
    {
      label: '调入人名称',
      prop: 'toUserName',
      type: 'autocomplete',
      placeholder: '输入调入人名称并选择',
      onSelect: (item, formModel) => handleUserSelect(item, formModel, 'toUserId', 'toUserName'),
      componentProps: {
        fetchSuggestions: queryUserSuggestions,
        triggerOnFocus: true,
        valueKey: 'value',
        clearable: true
      }
    },
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
    { label: '调拨原因', prop: 'reason', type: 'textarea', placeholder: '请输入调拨原因' },
    { label: '调出部门编号', prop: 'fromDepartmentId', hidden: true, default: () => (lockFromSide ? currentUser.value.departmentId : null) },
    { label: '调入部门编号', prop: 'toDepartmentId', hidden: true },
    { label: '调出人编号', prop: 'fromUserId', hidden: true, default: () => (lockFromSide ? currentUser.value.userId : null) },
    { label: '调入人编号', prop: 'toUserId', hidden: true },
    { label: '资产内部编号', prop: 'assetId', hidden: true }
  ]
})

function submitPayloadBuilder({ formModel }) {
  if (dataScope.value === DATA_SCOPES.GLOBAL) {
    formModel.fromDepartmentId = findDepartmentByName(formModel.fromDepartmentName)?.id ?? null
    formModel.fromUserId = findUserByName(formModel.fromUserName)?.id ?? null
  } else {
    formModel.fromDepartmentId = currentUser.value.departmentId
    formModel.fromUserId = currentUser.value.userId
  }

  formModel.toDepartmentId = findDepartmentByName(formModel.toDepartmentName)?.id ?? null
  formModel.toUserId = findUserByName(formModel.toUserName)?.id ?? null
  formModel.assetId = findAssetByName(formModel.assetName)?.id ?? null

  if (!formModel.toDepartmentId) {
    ElMessage.warning('请选择有效的调入部门名称')
    throw new Error('invalid to department')
  }

  if (!formModel.toUserId) {
    ElMessage.warning('请选择有效的调入人名称')
    throw new Error('invalid to user')
  }

  if (!formModel.assetId) {
    ElMessage.warning('请选择有效的资产名称')
    throw new Error('invalid asset')
  }

  return {
    fromDepartmentId: Number(formModel.fromDepartmentId),
    toDepartmentId: Number(formModel.toDepartmentId),
    fromUserId: Number(formModel.fromUserId),
    toUserId: Number(formModel.toUserId),
    reason: formModel.reason,
    itemList: [
      {
        assetId: Number(formModel.assetId)
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
        fromUserName: currentUser.value.realName,
        fromUserDepartmentName: currentUser.value.departmentName
      }),
      department: () => ({
        fromUserDepartmentName: currentUser.value.departmentName
      })
    }
  )
}

loadLookups()
</script>

<template>
  <ModuleScaffold
    title="资产转移"
    description="查询调拨记录并发起资产调拨，表单已改为按名称选择部门、人员和资产。"
    :filters="filters"
    :columns="columns"
    :form-fields="formFields"
    :detail-fields="detailFields"
    :submit-payload-builder="submitPayloadBuilder"
    :query-payload-builder="queryPayloadBuilder"
    :permissions="{ create: canCreateTransfer }"
    :api="transferApi"
  />
</template>
