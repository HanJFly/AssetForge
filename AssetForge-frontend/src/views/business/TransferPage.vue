<script setup>
import { computed } from 'vue'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { transferApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, DATA_SCOPES, getRoleDataScope, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

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
  { label: 'ID', prop: 'id', width: 80 },
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
      label: '调出部门 ID',
      prop: 'fromDepartmentId',
      placeholder: '请输入调出部门 ID',
      default: () => currentUser.value.departmentId,
      componentProps: lockFromSide ? { disabled: true } : {}
    },
    { label: '调入部门 ID', prop: 'toDepartmentId', placeholder: '请输入调入部门 ID' },
    {
      label: '调出人 ID',
      prop: 'fromUserId',
      placeholder: '请输入调出人 ID',
      default: () => currentUser.value.userId,
      componentProps: lockFromSide ? { disabled: true } : {}
    },
    { label: '调入人 ID', prop: 'toUserId', placeholder: '请输入调入人 ID' },
    { label: '资产 ID', prop: 'assetId', placeholder: '请输入资产 ID' },
    { label: '调拨原因', prop: 'reason', type: 'textarea', placeholder: '请输入调拨原因' }
  ]
})

function submitPayloadBuilder({ formModel }) {
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
</script>

<template>
  <ModuleScaffold
    title="资产调拨"
    description="查询调拨记录并发起资产调拨。"
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
