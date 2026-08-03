export const ROLE_CODES = {
  EMPLOYEE: 'EMPLOYEE',
  STOREKEEPER: 'STOREKEEPER',
  ASSET_ADMIN: 'ASSET_ADMIN',
  DEPT_MANAGER: 'DEPT_MANAGER'
}

export const ALL_ROLE_CODES = Object.values(ROLE_CODES)

export const DATA_SCOPES = {
  SELF: 'SELF',
  DEPARTMENT_TREE: 'DEPARTMENT_TREE',
  GLOBAL: 'GLOBAL'
}

export const ACTION_CODES = {
  ASSET_VIEW_ALL: 'asset:view:all',
  ASSET_REGISTER: 'asset:register',
  ASSET_LEDGER_VIEW: 'asset:ledger:view',
  APPROVAL_REVIEW: 'approval:review',
  REQUISITION_CREATE: 'requisition:create',
  REQUISITION_OUTBOUND: 'requisition:outbound',
  TRANSFER_CREATE: 'transfer:create',
  RETURN_CREATE: 'return:create',
  RETURN_INBOUND: 'return:inbound',
  SCRAP_CREATE: 'scrap:create',
  INVENTORY_CREATE: 'inventory:create',
  INVENTORY_EXECUTE: 'inventory:execute',
  LOSS_HANDLE: 'loss:handle',
  REPORT_VIEW: 'report:view',
  FILE_VIEW: 'file:view',
  USER_MANAGE: 'user:manage',
  ROLE_MANAGE: 'role:manage',
  SYSTEM_CONFIG: 'system:config',
  DEPARTMENT_MANAGE: 'department:manage',
  CATEGORY_MANAGE: 'category:manage'
}

export const ROLE_PERMISSION_MAP = {
  [ROLE_CODES.EMPLOYEE]: {
    dataScope: DATA_SCOPES.SELF,
    actions: [
      ACTION_CODES.REQUISITION_CREATE,
      ACTION_CODES.TRANSFER_CREATE,
      ACTION_CODES.RETURN_CREATE,
      ACTION_CODES.SCRAP_CREATE
    ]
  },
  [ROLE_CODES.STOREKEEPER]: {
    dataScope: DATA_SCOPES.GLOBAL,
    actions: [
      ACTION_CODES.ASSET_VIEW_ALL,
      ACTION_CODES.ASSET_LEDGER_VIEW,
      ACTION_CODES.REQUISITION_OUTBOUND,
      ACTION_CODES.RETURN_INBOUND,
      ACTION_CODES.INVENTORY_CREATE,
      ACTION_CODES.INVENTORY_EXECUTE,
      ACTION_CODES.REPORT_VIEW,
      ACTION_CODES.FILE_VIEW
    ]
  },
  [ROLE_CODES.ASSET_ADMIN]: {
    dataScope: DATA_SCOPES.GLOBAL,
    actions: Object.values(ACTION_CODES)
  },
  [ROLE_CODES.DEPT_MANAGER]: {
    dataScope: DATA_SCOPES.DEPARTMENT_TREE,
    actions: [
      ACTION_CODES.ASSET_VIEW_ALL,
      ACTION_CODES.ASSET_REGISTER,
      ACTION_CODES.ASSET_LEDGER_VIEW,
      ACTION_CODES.APPROVAL_REVIEW,
      ACTION_CODES.REQUISITION_CREATE,
      ACTION_CODES.TRANSFER_CREATE,
      ACTION_CODES.RETURN_CREATE,
      ACTION_CODES.SCRAP_CREATE,
      ACTION_CODES.INVENTORY_CREATE,
      ACTION_CODES.INVENTORY_EXECUTE,
      ACTION_CODES.REPORT_VIEW
    ]
  }
}

export const APP_ROUTE_DEFS = [
  {
    path: '/dashboard',
    name: 'dashboard',
    meta: { title: '工作台', icon: 'House', roles: ALL_ROLE_CODES, group: 'overview' }
  },
  {
    path: '/org/department',
    name: 'department',
    meta: {
      title: '部门管理',
      icon: 'OfficeBuilding',
      roles: [ROLE_CODES.ASSET_ADMIN],
      group: 'base'
    }
  },
  {
    path: '/org/category',
    name: 'category',
    meta: {
      title: '资产分类',
      icon: 'CollectionTag',
      roles: [ROLE_CODES.ASSET_ADMIN],
      group: 'base'
    }
  },
  {
    path: '/system/user',
    name: 'user',
    meta: {
      title: '用户管理',
      icon: 'User',
      roles: [ROLE_CODES.ASSET_ADMIN],
      group: 'base'
    }
  },
  {
    path: '/system/config',
    name: 'config',
    meta: {
      title: '系统配置',
      icon: 'Setting',
      roles: [ROLE_CODES.ASSET_ADMIN],
      group: 'base'
    }
  },
  {
    path: '/asset/list',
    name: 'asset',
    meta: { title: '资产管理', icon: 'Box', roles: ALL_ROLE_CODES, group: 'business' }
  },
  {
    path: '/approval/list',
    name: 'approval',
    meta: {
      title: '审批中心',
      icon: 'Checked',
      roles: [ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'business'
    }
  },
  {
    path: '/business/requisition',
    name: 'requisition',
    meta: {
      title: '资产申领',
      icon: 'ShoppingCart',
      roles: [ROLE_CODES.EMPLOYEE, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'business'
    }
  },
  {
    path: '/business/outbound',
    name: 'outbound',
    meta: {
      title: '出库作业',
      icon: 'Top',
      roles: [ROLE_CODES.STOREKEEPER, ROLE_CODES.ASSET_ADMIN],
      group: 'business'
    }
  },
  {
    path: '/business/transfer',
    name: 'transfer',
    meta: {
      title: '资产转移',
      icon: 'Switch',
      roles: [ROLE_CODES.EMPLOYEE, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'business'
    }
  },
  {
    path: '/business/return',
    name: 'return',
    meta: {
      title: '资产归还',
      icon: 'RefreshLeft',
      roles: [ROLE_CODES.EMPLOYEE, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'business'
    }
  },
  {
    path: '/business/inbound',
    name: 'inbound',
    meta: {
      title: '入库作业',
      icon: 'Bottom',
      roles: [ROLE_CODES.STOREKEEPER, ROLE_CODES.ASSET_ADMIN],
      group: 'business'
    }
  },
  {
    path: '/business/scrap',
    name: 'scrap',
    meta: {
      title: '资产报废',
      icon: 'DeleteFilled',
      roles: [ROLE_CODES.EMPLOYEE, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'business'
    }
  },
  {
    path: '/inventory/task',
    name: 'inventory-task',
    meta: {
      title: '盘点管理',
      icon: 'Tickets',
      roles: [ROLE_CODES.STOREKEEPER, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'report'
    }
  },
  {
    path: '/inventory/loss',
    name: 'loss-order',
    meta: {
      title: '盘亏处理',
      icon: 'Warning',
      roles: [ROLE_CODES.ASSET_ADMIN],
      group: 'report'
    }
  },
  {
    path: '/report/index',
    name: 'report',
    meta: {
      title: '报表与折旧',
      icon: 'Histogram',
      roles: [ROLE_CODES.STOREKEEPER, ROLE_CODES.ASSET_ADMIN, ROLE_CODES.DEPT_MANAGER],
      group: 'report'
    }
  },
  {
    path: '/file/index',
    name: 'file',
    meta: {
      title: '文件附件',
      icon: 'FolderOpened',
      roles: [ROLE_CODES.STOREKEEPER, ROLE_CODES.ASSET_ADMIN],
      group: 'report'
    }
  }
]

export const MENU_GROUPS = [
  { key: 'overview', title: '总览' },
  { key: 'base', title: '组织与基础' },
  { key: 'business', title: '业务操作' },
  { key: 'report', title: '盘点与报表' }
]

export function roleAllows(routeRoles, roleCode) {
  if (!routeRoles || routeRoles.length === 0) return true
  return Boolean(roleCode) && routeRoles.includes(roleCode)
}

export function getRoleDataScope(roleCode) {
  return ROLE_PERMISSION_MAP[roleCode]?.dataScope || DATA_SCOPES.SELF
}

export function roleHasAction(roleCode, actionCode) {
  if (!actionCode) return true
  return Boolean(roleCode) && ROLE_PERMISSION_MAP[roleCode]?.actions?.includes(actionCode)
}

export function getAccessibleRouteDefs(roleCode) {
  return APP_ROUTE_DEFS.filter((route) => roleAllows(route.meta?.roles, roleCode))
}

export function getVisibleMenus(roleCode) {
  return MENU_GROUPS.map((group) => ({
    ...group,
    items: APP_ROUTE_DEFS.filter(
      (route) => route.meta?.group === group.key && roleAllows(route.meta?.roles, roleCode)
    ).map((route) => ({
      title: route.meta.title,
      path: route.path,
      icon: route.meta.icon,
      roles: route.meta.roles
    }))
  })).filter((group) => group.items.length > 0)
}

export function findFirstAccessiblePath(routesOrDefs, roleCode) {
  const defs = Array.isArray(routesOrDefs)
    ? routesOrDefs.flatMap((route) =>
        route.children?.length
          ? route.children.map((child) => ({
              path: child.path.startsWith('/') ? child.path : `/${child.path}`,
              meta: child.meta
            }))
          : route.path
            ? [{ path: route.path, meta: route.meta }]
            : []
      )
    : APP_ROUTE_DEFS

  const firstRoute = defs.find((route) => {
    if (route.path === '/login' || route.path === '/select-role') return false
    return roleAllows(route.meta?.roles, roleCode)
  })

  return firstRoute?.path || '/dashboard'
}
