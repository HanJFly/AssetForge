import { reactive } from 'vue'

import { authApi } from '@/api'

const TOKEN_KEY = 'assetforge-token'
const USER_KEY = 'assetforge-user'
const ROLE_KEY = 'assetforge-role'

function parseStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch (_error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const authState = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: parseStoredUser(),
  selectedRole: (() => {
    const raw = localStorage.getItem(ROLE_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch (_error) {
      localStorage.removeItem(ROLE_KEY)
      return null
    }
  })()
})

export function getToken() {
  return authState.token || ''
}

export function isLoggedIn() {
  return Boolean(getToken())
}

export function setToken(token) {
  authState.token = token || ''

  if (authState.token) {
    localStorage.setItem(TOKEN_KEY, authState.token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function setUser(user) {
  authState.user = user || null

  if (authState.user) {
    localStorage.setItem(USER_KEY, JSON.stringify(authState.user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export function getSelectedRole() {
  return authState.selectedRole
}

export function setSelectedRole(role) {
  authState.selectedRole = role || null

  if (authState.selectedRole) {
    localStorage.setItem(ROLE_KEY, JSON.stringify(authState.selectedRole))
  } else {
    localStorage.removeItem(ROLE_KEY)
  }
}

export function setSession({ token, user }) {
  setToken(token)
  setUser(user)
  setSelectedRole(null)
}

export function clearSession() {
  setToken('')
  setUser(null)
  setSelectedRole(null)
}

export async function restoreUser() {
  if (!isLoggedIn()) return null

  try {
    const payload = await authApi.me({})
    const user = payload?.data ?? null
    if (user) {
      setUser(user)
    }
    return user || authState.user
  } catch (error) {
    const status = error?.response?.status
    const code = error?.code
    const bizCode = error?.data?.code

    if (status === 401 || code === 401 || bizCode === 401) {
      clearSession()
      throw error
    }

    return authState.user
  }
}
