import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '@/layout/AppLayout.vue'
import LoginView from '@/views/LoginView.vue'
import SelectRoleView from '@/views/SelectRoleView.vue'
import DashboardView from '@/views/DashboardView.vue'
import DepartmentPage from '@/views/org/DepartmentPage.vue'
import CategoryPage from '@/views/org/CategoryPage.vue'
import UserPage from '@/views/system/UserPage.vue'
import ConfigPage from '@/views/system/ConfigPage.vue'
import AssetPage from '@/views/asset/AssetPage.vue'
import ApprovalPage from '@/views/approval/ApprovalPage.vue'
import RequisitionPage from '@/views/business/RequisitionPage.vue'
import OutboundPage from '@/views/business/OutboundPage.vue'
import TransferPage from '@/views/business/TransferPage.vue'
import ReturnPage from '@/views/business/ReturnPage.vue'
import InboundPage from '@/views/business/InboundPage.vue'
import ScrapPage from '@/views/business/ScrapPage.vue'
import InventoryTaskPage from '@/views/inventory/InventoryTaskPage.vue'
import LossOrderPage from '@/views/inventory/LossOrderPage.vue'
import ReportPage from '@/views/report/ReportPage.vue'
import FilePage from '@/views/file/FilePage.vue'
import { getSelectedRole, isLoggedIn } from '@/utils/auth'
import { APP_ROUTE_DEFS, findFirstAccessiblePath, roleAllows } from '@/utils/role-access'

const routeComponents = {
  dashboard: DashboardView,
  department: DepartmentPage,
  category: CategoryPage,
  user: UserPage,
  config: ConfigPage,
  asset: AssetPage,
  approval: ApprovalPage,
  requisition: RequisitionPage,
  outbound: OutboundPage,
  transfer: TransferPage,
  return: ReturnPage,
  inbound: InboundPage,
  scrap: ScrapPage,
  'inventory-task': InventoryTaskPage,
  'loss-order': LossOrderPage,
  report: ReportPage,
  file: FilePage
}

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录' }
  },
  {
    path: '/select-role',
    name: 'select-role',
    component: SelectRoleView,
    meta: { title: '选择角色' }
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: APP_ROUTE_DEFS.map((route) => ({
      path: route.path.replace(/^\//, ''),
      name: route.name,
      component: routeComponents[route.name],
      meta: route.meta
    }))
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'AssetForge'} - AssetForge`

  if (to.path === '/login') {
    if (isLoggedIn()) {
      next('/select-role')
      return
    }
    next()
    return
  }

  if (!isLoggedIn()) {
    next('/login')
    return
  }

  if (to.path === '/select-role') {
    next()
    return
  }

  const selectedRole = getSelectedRole()
  if (!selectedRole?.code) {
    next({ path: '/select-role', query: { redirect: to.fullPath } })
    return
  }

  if (!roleAllows(to.meta?.roles, selectedRole.code)) {
    next(findFirstAccessiblePath(routes, selectedRole.code))
    return
  }

  next()
})

export default router
