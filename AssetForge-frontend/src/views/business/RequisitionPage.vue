<script setup>
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { requisitionApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const approvalStatusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const baseFilters = [
  { label: '申领单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '申请人', prop: 'applicantName', placeholder: '按申请人模糊查询' },
  { label: '申请部门', prop: 'departmentName', placeholder: '按部门模糊查询' },
  { label: '审批人', prop: 'approverName', placeholder: '按审批人模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: 'ID', prop: 'id', width: 80 },
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

const outboundForm = reactive({
  id: 1,
  confirmRemark: '已发放',
  itemId: 1,
  assetId: 1
})

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canCreateRequisition = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.REQUISITION_CREATE))
const canConfirmOutbound = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.REQUISITION_OUTBOUND))
const currentUser = computed(() => getCurrentUserProfile())

const filters = computed(() => {
  if (canConfirmOutbound.value && !canCreateRequisition.value) {
    return baseFilters
  }

  return baseFilters.map((field) => ({
    ...field,
    hidden: ['applicantName', 'departmentName'].includes(field.prop)
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
    label: '当前申请部门',
    prop: 'currentDepartmentName',
    default: () => currentUser.value.departmentName || '当前所属部门',
    componentProps: { disabled: true }
  },
  { label: '申请原因', prop: 'reason', type: 'textarea', placeholder: '请输入申请原因' },
  { label: '期望日期', prop: 'expectedDate', type: 'date' },
  { label: '分类 ID', prop: 'categoryId', placeholder: '请输入分类 ID' },
  { label: '数量', prop: 'quantity', type: 'number', default: 1 }
])

function submitPayloadBuilder({ formModel }) {
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
      scope: canConfirmOutbound.value && !canCreateRequisition.value ? 'GLOBAL' : 'SELF',
      self: () => ({
        applicantName: currentUser.value.realName,
        departmentName: currentUser.value.departmentName
      })
    }
  )
}

async function submitOutbound() {
  try {
    await requisitionApi.confirmOutbound({
      id: Number(outboundForm.id),
      confirmRemark: outboundForm.confirmRemark,
      itemList: [
        {
          itemId: Number(outboundForm.itemId),
          assetId: Number(outboundForm.assetId)
        }
      ]
    })
    ElMessage.success('出库确认已提交')
  } catch (error) {
    ElMessage.error(error?.message || '出库确认失败')
  }
}
</script>

<template>
  <div class="page-container">
    <ModuleScaffold
      title="资产申领"
      description="查询申领记录、发起申领并处理出库确认。"
      :filters="filters"
      :columns="columns"
      :form-fields="formFields"
      :detail-fields="detailFields"
      :submit-payload-builder="submitPayloadBuilder"
      :query-payload-builder="queryPayloadBuilder"
      :permissions="{ create: canCreateRequisition }"
      :api="requisitionApi"
    />

    <el-card v-if="canConfirmOutbound" shadow="never" class="page-card">
      <template #header><span>出库确认</span></template>
      <el-form label-width="100px">
        <el-form-item label="申领单 ID">
          <el-input-number v-model="outboundForm.id" class="full-width" />
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input v-model="outboundForm.confirmRemark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="明细 ID">
          <el-input-number v-model="outboundForm.itemId" class="full-width" />
        </el-form-item>
        <el-form-item label="资产 ID">
          <el-input-number v-model="outboundForm.assetId" class="full-width" />
        </el-form-item>
      </el-form>
      <div class="toolbar-row">
        <el-button type="primary" @click="submitOutbound">提交出库确认</el-button>
      </div>
    </el-card>
  </div>
</template>
