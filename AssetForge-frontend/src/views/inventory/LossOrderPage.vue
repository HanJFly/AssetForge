<script setup>
import { computed } from 'vue'

import ModuleScaffold from '@/components/ModuleScaffold.vue'
import { inventoryApi } from '@/api'
import { authState } from '@/utils/auth'
import { ACTION_CODES, roleHasAction } from '@/utils/role-access'

const compensationStatusOptions = [
  { label: '待赔偿', value: 'PENDING_COMPENSATION' },
  { label: '已赔偿', value: 'COMPENSATED' },
  { label: '免责中', value: 'EXEMPTING' },
  { label: '已免责', value: 'EXEMPTED' }
]

const filters = [
  { label: '盘亏单号', prop: 'orderNo', placeholder: '按单号查询' },
  { label: '赔偿状态', prop: 'compensationStatus', type: 'select', options: compensationStatusOptions }
]

const columns = [
  { label: 'ID', prop: 'id', width: 80 },
  { label: '盘亏单号', prop: 'orderNo', minWidth: 160 },
  { label: '资产编码', prop: 'assetCode', minWidth: 150 },
  { label: '资产名称', prop: 'assetName', minWidth: 160 },
  { label: '赔偿状态', prop: 'compensationStatus', width: 150 },
  { label: '创建时间', prop: 'createdAt', minWidth: 170 }
]

const formFields = [
  { label: 'ID', prop: 'id', placeholder: '处理时必填' },
  { label: '处理类型', prop: 'handleType', placeholder: 'COMPENSATE / EXEMPT' },
  { label: '处理金额', prop: 'handleAmount', type: 'number', default: 0 },
  { label: '处理意见', prop: 'handlingRemark', type: 'textarea', placeholder: '请输入处理意见' }
]

const detailFields = [
  { label: '盘亏单号', prop: 'orderNo' },
  { label: '资产编码', prop: 'assetCode' },
  { label: '责任人', prop: 'responsibleUserName' },
  { label: '责任部门', prop: 'responsibleDepartmentName' },
  { label: '赔偿状态', prop: 'compensationStatus' }
]

const api = {
  page: inventoryApi.lossPage,
  detail: inventoryApi.lossDetail,
  create: inventoryApi.lossHandle,
  update: inventoryApi.lossHandle
}

const selectedRoleCode = computed(() => authState.selectedRole?.code || '')
const canHandleLoss = computed(() => roleHasAction(selectedRoleCode.value, ACTION_CODES.LOSS_HANDLE))
</script>

<template>
  <ModuleScaffold
    title="盘亏处理"
    description="查询盘亏记录并处理赔偿或免责。"
    :filters="filters"
    :columns="columns"
    :form-fields="formFields"
    :detail-fields="detailFields"
    :permissions="{ create: canHandleLoss, edit: canHandleLoss }"
    :api="api"
  />
</template>
