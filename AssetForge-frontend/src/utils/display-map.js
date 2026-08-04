const approvalStatusMap = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}

const approvalTypeMap = {
  ASSET: '资产登记',
  APPLY: '资产申领',
  REQUISITION: '资产申领',
  TRANSFER: '资产转移',
  RETURN: '资产归还',
  SCRAP: '资产报废'
}

const businessTypeMap = {
  asset: '资产登记',
  requisition_order: '资产申领',
  transfer_order: '资产转移',
  return_order: '资产归还',
  scrap_order: '资产报废',
  ASSET: '资产登记',
  REQUISITION_ORDER: '资产申领',
  TRANSFER_ORDER: '资产转移',
  RETURN_ORDER: '资产归还',
  SCRAP_ORDER: '资产报废'
}

const decisionMap = {
  APPROVED: '通过',
  REJECTED: '驳回'
}

const assetStatusMap = {
  PENDING: '待审批',
  STOCK: '库存',
  ASSIGNED: '已领用',
  SCRAPPED: '已报废',
  LOST: '盘亏'
}

const sourceTypeMap = {
  PURCHASE: '采购',
  LEASE: '租赁'
}

const assetConditionMap = {
  NORMAL: '正常',
  MINOR_DAMAGE: '轻微损坏',
  MAJOR_DAMAGE: '严重损坏'
}

const inventoryResultMap = {
  NORMAL: '正常',
  LOSS: '盘亏',
  GAIN: '盘盈',
  MISMATCH: '账实不符'
}

const inventoryTaskStatusMap = {
  PENDING: '待开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成'
}

const scopeTypeMap = {
  ALL: '全部资产',
  DEPARTMENT: '按部门',
  CATEGORY: '按分类'
}

export function formatDisplay(map, value) {
  if (value == null || value === '') {
    return '-'
  }

  return map[value] ?? String(value)
}

export function formatApprovalStatus(value) {
  return formatDisplay(approvalStatusMap, value)
}

export function formatApprovalType(value) {
  return formatDisplay(approvalTypeMap, value)
}

export function formatBusinessType(value) {
  return formatDisplay(businessTypeMap, value)
}

export function formatDecision(value) {
  return formatDisplay(decisionMap, value)
}

export function formatAssetStatus(value) {
  return formatDisplay(assetStatusMap, value)
}

export function formatSourceType(value) {
  return formatDisplay(sourceTypeMap, value)
}

export function formatAssetCondition(value) {
  return formatDisplay(assetConditionMap, value)
}

export function formatInventoryResult(value) {
  return formatDisplay(inventoryResultMap, value)
}

export function formatInventoryTaskStatus(value) {
  return formatDisplay(inventoryTaskStatusMap, value)
}

export function formatScopeType(value) {
  return formatDisplay(scopeTypeMap, value)
}
