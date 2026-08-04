import http from './http'

const apiEnabled = import.meta.env.VITE_ENABLE_API === 'true'

const implementedEndpoints = new Set([
  '/auth/login',
  '/auth/me',
  '/auth/select-role',
  '/department/getAll',
  '/department/tree',
  '/department/page',
  '/department/detail',
  '/department/create',
  '/department/update',
  '/department/delete',
  '/category/tree',
  '/category/page',
  '/category/detail',
  '/category/create',
  '/category/update',
  '/category/delete',
  '/user/page',
  '/user/detail',
  '/user/create',
  '/user/update',
  '/user/reset-password',
  '/user/delete',
  '/user/role/list',
  '/system/config/detail',
  '/system/config/update',
  '/asset/page',
  '/asset/detail',
  '/asset/create',
  '/asset/create-with-files',
  '/asset/update',
  '/asset/delete',
  '/asset/ledger/page',
  '/asset/barcode/detail',
  '/approval/todo/page',
  '/approval/done/page',
  '/approval/detail',
  '/approval/action',
  '/approval/transfer',
  '/approval/approvers',
  '/receive-order/create',
  '/receive-order/page',
  '/receive-order/detail',
  '/receive-order/confirm-outbound',
  '/transfer-order/create',
  '/transfer-order/page',
  '/transfer-order/detail',
  '/return-order/create',
  '/return-order/page',
  '/return-order/detail',
  '/return-order/confirm-inbound',
  '/scrapOrder/create',
  '/scrapOrder/page',
  '/scrapOrder/detail',
  '/inventoryTask/create',
  '/inventoryTask/page',
  '/inventoryTask/detail',
  '/inventoryTask/report',
  '/inventoryTask/conclusion',
  '/inventoryDetail/page',
  '/inventoryDetail/submit',
  '/lossOrder/page',
  '/lossOrder/detail',
  '/lossOrder/handle',
  '/report/asset-detail',
  '/report/ledger-snapshot/page',
  '/report/monthly-summary',
  '/depreciationRunLog/execute',
  '/depreciationRunLog/page',
  '/file/upload',
  '/file/list',
  '/file/bind'
])

const post = (url, data = {}) => {
  if (!apiEnabled || !implementedEndpoints.has(url)) {
    return Promise.resolve({
      code: 200,
      msg: 'mock success',
      data: null,
      mock: true
    })
  }

  const isFormData = typeof FormData !== 'undefined' && data instanceof FormData

  return http.post(
    url,
    data,
    isFormData
      ? {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      : undefined
  )
}

const upload = (url, file, bizType = '') => {
  if (!apiEnabled || !implementedEndpoints.has(url)) {
    return Promise.resolve({
      code: 200,
      msg: 'mock success',
      data: {
        id: Date.now(),
        fileName: file?.name || 'mock-file',
        bizType,
        fileUrl: file?.name || 'mock-file'
      },
      mock: true
    })
  }

  const formData = new FormData()
  formData.append('file', file)
  if (bizType) {
    formData.append('bizType', bizType)
  }

  return http.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const authApi = {
  login: (data) => post('/auth/login', data),
  me: (data) => post('/auth/me', data),
  selectRole: (data) => post('/auth/select-role', data),
  logout: (data) => post('/auth/logout', data)
}

export const departmentApi = {
  getAll: (data) => post('/department/getAll', data),
  tree: (data) => post('/department/tree', data),
  page: (data) => post('/department/page', data),
  detail: (data) => post('/department/detail', data),
  create: (data) => post('/department/create', data),
  update: (data) => post('/department/update', data),
  remove: (data) => post('/department/delete', data)
}

export const categoryApi = {
  tree: (data) => post('/category/tree', data),
  page: (data) => post('/category/page', data),
  detail: (data) => post('/category/detail', data),
  create: (data) => post('/category/create', data),
  update: (data) => post('/category/update', data),
  remove: (data) => post('/category/delete', data)
}

export const userApi = {
  page: (data) => post('/user/page', data),
  detail: (data) => post('/user/detail', data),
  create: (data) => post('/user/create', data),
  update: (data) => post('/user/update', data),
  remove: (data) => post('/user/delete', data),
  resetPassword: (data) => post('/user/reset-password', data)
}

export const roleApi = {
  list: (data) => post('/user/role/list', data)
}

export const configApi = {
  detail: (data) => post('/system/config/detail', data),
  update: (data) => post('/system/config/update', data)
}

export const assetApi = {
  page: (data) => post('/asset/page', data),
  detail: (data) => post('/asset/detail', data),
  create: (data) => post('/asset/create', data),
  createWithFiles: (data) => post('/asset/create-with-files', data),
  update: (data) => post('/asset/update', data),
  remove: (data) => post('/asset/delete', data),
  ledgerPage: (data) => post('/asset/ledger/page', data),
  barcodeDetail: (data) => post('/asset/barcode/detail', data)
}

export const approvalApi = {
  todoPage: (data) => post('/approval/todo/page', data),
  donePage: (data) => post('/approval/done/page', data),
  detail: (data) => post('/approval/detail', data),
  action: (data) => post('/approval/action', data),
  transfer: (data) => post('/approval/transfer', data),
  approvers: () => post('/approval/approvers')
}

export const requisitionApi = {
  page: (data) => post('/receive-order/page', data),
  detail: (data) => post('/receive-order/detail', data),
  create: (data) => post('/receive-order/create', data),
  confirmOutbound: (data) => post('/receive-order/confirm-outbound', data)
}

export const transferApi = {
  page: (data) => post('/transfer-order/page', data),
  detail: (data) => post('/transfer-order/detail', data),
  create: (data) => post('/transfer-order/create', data)
}

export const returnApi = {
  page: (data) => post('/return-order/page', data),
  detail: (data) => post('/return-order/detail', data),
  create: (data) => post('/return-order/create', data),
  confirmInbound: (data) => post('/return-order/confirm-inbound', data)
}

export const scrapApi = {
  page: (data) => post('/scrapOrder/page', data),
  detail: (data) => post('/scrapOrder/detail', data),
  create: (data) => post('/scrapOrder/create', data)
}

export const inventoryApi = {
  taskPage: (data) => post('/inventoryTask/page', data),
  taskDetail: (data) => post('/inventoryTask/detail', data),
  taskCreate: (data) => post('/inventoryTask/create', data),
  detailPage: (data) => post('/inventoryDetail/page', data),
  detailSubmit: (data) => post('/inventoryDetail/submit', data),
  report: (data) => post('/inventoryTask/report', data),
  conclusion: (data) => post('/inventoryTask/conclusion', data),
  lossPage: (data) => post('/lossOrder/page', data),
  lossDetail: (data) => post('/lossOrder/detail', data),
  lossHandle: (data) => post('/lossOrder/handle', data)
}

export const reportApi = {
  assetDetail: (data) => post('/report/asset-detail', data),
  ledgerSnapshotPage: (data) => post('/report/ledger-snapshot/page', data),
  monthlySummary: (data) => post('/report/monthly-summary', data),
  executeDepreciation: (data) => post('/depreciationRunLog/execute', data),
  depreciationPage: (data) => post('/depreciationRunLog/page', data)
}

export const fileApi = {
  upload: (file, bizType) => upload('/file/upload', file, bizType),
  list: (data) => post('/file/list', data),
  bind: (data) => post('/file/bind', data)
}

export { apiEnabled }
