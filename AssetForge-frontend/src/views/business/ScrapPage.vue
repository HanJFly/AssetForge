<script setup>
import { computed } from 'vue'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { scrapApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, DATA_SCOPES, getRoleDataScope, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const baseFilters = [
  { label: '报废单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '申请人', prop: 'applicantName', placeholder: '按申请人模糊查询' },
  { label: '申请人工号', prop: 'applicantEmployeeNo', placeholder: '按工号模糊查询' },
  { label: '申请部门', prop: 'applicantDepartmentName', placeholder: '按部门模糊查询' },
  { label: '审批人', prop: 'approvalName', placeholder: '按审批人模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: '报废单号', prop: 'orderNo', minWidth: 160 },
  { label: '申请人', prop: 'applicantName', minWidth: 120 },
  { label: '审批状态', prop: 'approvalStatus', width: 120 },
  { label: '创建时间', prop: 'createdAt', minWidth: 170 }
]

const detailFields = [
  { label: '报废单号', prop: 'orderNo' },
  { label: '申请人', prop: 'applicantName' },
  { label: '报废原因', prop: 'reason' },
  { label: '审批状态', prop: 'approvalStatus' }
]

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const dataScope = computed(() => getRoleDataScope(selectedRoleCode.value))
const canCreateScrap = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.SCRAP_CREATE))
const currentUser = computed(() => getCurrentUserProfile())

const filters = computed(() => {
  if (dataScope.value === DATA_SCOPES.GLOBAL) {
    return baseFilters
  }

  return baseFilters.map((field) => ({
    ...field,
    hidden: ['applicantName', 'applicantEmployeeNo', 'applicantDepartmentName'].includes(field.prop)
  }))
})

const formFields = computed(() => [
  {
    label: '当前申请人',
    prop: 'currentApplicantName',
    default: () => currentUser.value.realName || '当前登录用户',
    componentProps: { disabled: true }
  },
  {
    label: '当前工号',
    prop: 'currentApplicantEmployeeNo',
    default: () => currentUser.value.employeeNo || '未配置工号',
    componentProps: { disabled: true }
  },
  {
    label: '当前申请部门',
    prop: 'currentApplicantDepartmentName',
    default: () => currentUser.value.departmentName || '当前所属部门',
    componentProps: { disabled: true }
  },
  { label: '资产内部编号', prop: 'assetId', placeholder: '请输入资产内部编号' },
  { label: '附件编号列表', prop: 'attachmentIdsText', placeholder: '如 1,2,3' },
  { label: '报废原因', prop: 'reason', type: 'textarea', placeholder: '请输入报废原因' }
])

function parseIdList(text) {
  if (!text) return []

  return String(text)
    .split(',')
    .map((item) => Number(item.trim()))
    .filter(Boolean)
}

function submitPayloadBuilder({ formModel }) {
  return {
    reason: formModel.reason,
    itemList: [
      {
        assetId: Number(formModel.assetId)
      }
    ],
    attachmentIds: parseIdList(formModel.attachmentIdsText)
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
        applicantEmployeeNo: currentUser.value.employeeNo,
        applicantDepartmentName: currentUser.value.departmentName
      }),
      department: () => ({
        applicantDepartmentName: currentUser.value.departmentName
      })
    }
  )
}
</script>

<template>
  <ModuleScaffold
    title="资产报废"
    description="查询报废申请并发起报废流程。"
    :filters="filters"
    :columns="columns"
    :form-fields="formFields"
    :detail-fields="detailFields"
    :submit-payload-builder="submitPayloadBuilder"
    :query-payload-builder="queryPayloadBuilder"
    :permissions="{ create: canCreateScrap }"
    :api="scrapApi"
  />
</template>
