<script setup>
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { returnApi } from '@/api'
import { authState } from '@/utils/auth'
import { buildScopedQuery, getCurrentUserProfile } from '@/utils/data-scope'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

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

const baseFilters = [
  { label: '归还单号', prop: 'orderNo', placeholder: '按单号前缀查询' },
  { label: '归还人', prop: 'returnUserName', placeholder: '按归还人模糊查询' },
  { label: '工号', prop: 'returnUserEmployeeNo', placeholder: '按工号模糊查询' },
  { label: '归还部门', prop: 'returnUserDepartmentName', placeholder: '按部门模糊查询' },
  { label: '审批状态', prop: 'approvalStatus', type: 'select', options: approvalStatusOptions },
  { label: '开始日期', prop: 'startDate', type: 'date' },
  { label: '结束日期', prop: 'endDate', type: 'date' }
]

const columns = [
  { label: 'ID', prop: 'id', width: 80 },
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

const inboundForm = reactive({
  id: 1,
  inboundRemark: '已完成入库验收',
  itemId: 1,
  assetId: 1
})

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canCreateReturn = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.RETURN_CREATE))
const canConfirmInbound = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.RETURN_INBOUND))
const currentUser = computed(() => getCurrentUserProfile())

const filters = computed(() => {
  if (canConfirmInbound.value && !canCreateReturn.value) {
    return baseFilters
  }

  return baseFilters.map((field) => ({
    ...field,
    hidden: ['returnUserName', 'returnUserEmployeeNo', 'returnUserDepartmentName'].includes(field.prop)
  }))
})

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
  { label: '资产 ID', prop: 'assetId', placeholder: '请输入资产 ID' },
  { label: '归还状态', prop: 'assetCondition', type: 'select', options: conditionOptions },
  { label: '状态说明', prop: 'conditionRemark', type: 'textarea', placeholder: '请输入状态说明' }
])

function submitPayloadBuilder({ formModel }) {
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
      scope: canConfirmInbound.value && !canCreateReturn.value ? 'GLOBAL' : 'SELF',
      self: () => ({
        returnUserName: currentUser.value.realName,
        returnUserEmployeeNo: currentUser.value.employeeNo,
        returnUserDepartmentName: currentUser.value.departmentName
      })
    }
  )
}

async function submitInbound() {
  try {
    await returnApi.confirmInbound({
      id: Number(inboundForm.id),
      inboundRemark: inboundForm.inboundRemark,
      itemList: [
        {
          itemId: Number(inboundForm.itemId),
          assetId: Number(inboundForm.assetId)
        }
      ]
    })
    ElMessage.success('入库确认已提交')
  } catch (error) {
    ElMessage.error(error?.message || '入库确认失败')
  }
}
</script>

<template>
  <div class="page-container">
    <ModuleScaffold
      title="资产归还"
      description="查询归还记录、发起归还并处理入库确认。"
      :filters="filters"
      :columns="columns"
      :form-fields="formFields"
      :detail-fields="detailFields"
      :submit-payload-builder="submitPayloadBuilder"
      :query-payload-builder="queryPayloadBuilder"
      :permissions="{ create: canCreateReturn }"
      :api="returnApi"
    />

    <el-card v-if="canConfirmInbound" shadow="never" class="page-card">
      <template #header><span>入库确认</span></template>
      <el-form label-width="100px">
        <el-form-item label="归还单 ID">
          <el-input-number v-model="inboundForm.id" class="full-width" />
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input v-model="inboundForm.inboundRemark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="明细 ID">
          <el-input-number v-model="inboundForm.itemId" class="full-width" />
        </el-form-item>
        <el-form-item label="资产 ID">
          <el-input-number v-model="inboundForm.assetId" class="full-width" />
        </el-form-item>
      </el-form>
      <div class="toolbar-row">
        <el-button type="primary" @click="submitInbound">提交入库确认</el-button>
      </div>
    </el-card>
  </div>
</template>
