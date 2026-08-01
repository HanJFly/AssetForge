import { authState } from '@/utils/auth'
import { DATA_SCOPES } from '@/utils/role-access'

function isPresent(value) {
  return value !== null && value !== undefined && value !== ''
}

function normalizeObject(source = {}) {
  return Object.fromEntries(Object.entries(source).filter(([, value]) => isPresent(value)))
}

export function getCurrentUserProfile() {
  const user = authState.user || {}

  return {
    userId: user.id ?? user.userId ?? null,
    realName: user.realName ?? user.name ?? user.username ?? '',
    employeeNo: user.employeeNo ?? '',
    departmentId: user.departmentId ?? null,
    departmentName: user.departmentName ?? ''
  }
}

export function buildScopedQuery(baseQuery = {}, options = {}) {
  const { scope = DATA_SCOPES.GLOBAL, self = {}, department = {} } = options
  const query = { ...baseQuery }

  if (scope === DATA_SCOPES.SELF) {
    return {
      ...query,
      ...normalizeObject(typeof self === 'function' ? self() : self)
    }
  }

  if (scope === DATA_SCOPES.DEPARTMENT_TREE) {
    return {
      ...query,
      ...normalizeObject(typeof department === 'function' ? department() : department)
    }
  }

  return query
}
