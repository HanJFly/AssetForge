<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { departmentApi, roleApi, userApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'

const loading = ref(false)
const pageRows = ref([])
const pageTotal = ref(0)
const detailResult = ref(null)
const roleResult = ref([])
const departmentOptions = ref([])
const currentUser = ref(null)
const detailSectionRef = ref(null)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const resetDialogVisible = ref(false)
const createSubmitting = ref(false)
const editSubmitting = ref(false)
const resetSubmitting = ref(false)

const roleOptions = [
  { id: 1, label: '普通员工' },
  { id: 2, label: '库管员' },
  { id: 3, label: '资产管理员' },
  { id: 4, label: '部门管理员' }
]

const roleLabelMap = computed(() => {
  const map = new Map()
  roleOptions.forEach((item) => {
    map.set(item.id, item.label)
  })
  return map
})

const departmentMapById = computed(() => {
  const map = new Map()
  departmentOptions.value.forEach((item) => {
    if (item?.id != null) {
      map.set(Number(item.id), item)
    }
  })
  return map
})

const pageForm = reactive({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  departmentId: null,
  departmentName: '',
  status: ''
})

const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  employeeNo: '',
  phone: '',
  email: '',
  departmentId: null,
  status: 'ACTIVE',
  roleIds: []
})

const editForm = reactive({
  id: null,
  username: '',
  realName: '',
  employeeNo: '',
  phone: '',
  email: '',
  departmentId: null,
  status: 'ACTIVE',
  roleIds: []
})

const resetForm = reactive({
  id: null,
  newPassword: ''
})

function requestWrap(action, successMessage = '') {
  loading.value = true
  return action()
    .then((payload) => {
      if (successMessage) {
        ElMessage.success(successMessage)
      }
      return payload
    })
    .catch((error) => {
      ElMessage.error(error?.message || '接口调用失败，请检查后端返回结果')
      return null
    })
    .finally(() => {
      loading.value = false
    })
}

function normalizeName(value) {
  return String(value || '').trim().toLowerCase()
}

function resetCreateForm() {
  Object.assign(createForm, {
    username: '',
    password: '',
    realName: '',
    employeeNo: '',
    phone: '',
    email: '',
    departmentId: null,
    status: 'ACTIVE',
    roleIds: []
  })
}

function resetEditForm() {
  Object.assign(editForm, {
    id: null,
    username: '',
    realName: '',
    employeeNo: '',
    phone: '',
    email: '',
    departmentId: null,
    status: 'ACTIVE',
    roleIds: []
  })
}

function resetResetForm() {
  Object.assign(resetForm, {
    id: null,
    newPassword: ''
  })
}

function fillEditForm(data = {}) {
  Object.assign(editForm, {
    id: data.id ?? null,
    username: data.username ?? '',
    realName: data.realName ?? '',
    employeeNo: data.employeeNo ?? '',
    phone: data.phone ?? '',
    email: data.email ?? '',
    departmentId: data.departmentId ?? null,
    status: data.status ?? 'ACTIVE',
    roleIds: Array.isArray(data.roleIds) ? [...data.roleIds] : []
  })
}

function formatRoleNames(row = {}) {
  if (Array.isArray(row.roleNames) && row.roleNames.length) {
    return row.roleNames.join('、')
  }

  if (Array.isArray(row.roleIds) && row.roleIds.length) {
    return row.roleIds.map((id) => roleLabelMap.value.get(id) || `角色${id}`).join('、')
  }

  return '-'
}

function buildDepartmentSuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return departmentOptions.value
    .filter((item) => !keyword || normalizeName(item.name).includes(keyword))
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function queryDepartmentSuggestions(queryString, callback) {
  callback(buildDepartmentSuggestions(queryString))
}

function syncQueryDepartmentById() {
  if (pageForm.departmentId == null || pageForm.departmentId === '') {
    pageForm.departmentName = ''
    return
  }

  const department = departmentMapById.value.get(Number(pageForm.departmentId))
  pageForm.departmentName = department?.name || ''
}

function syncQueryDepartmentByName() {
  const normalized = normalizeName(pageForm.departmentName)
  if (!normalized) {
    pageForm.departmentId = null
    return
  }

  const matched = departmentOptions.value.find((item) => normalizeName(item.name) === normalized)
  if (matched) {
    pageForm.departmentId = matched.id
  }
}

function handleDepartmentSelect(item) {
  pageForm.departmentId = item.id
  pageForm.departmentName = item.value
}

async function loadDepartments() {
  const payload = await requestWrap(() => departmentApi.getAll({}))
  departmentOptions.value = Array.isArray(payload?.data) ? payload.data : []
}

async function loadPage() {
  if (pageForm.departmentName.trim()) {
    const matched = departmentOptions.value.find(
      (item) => normalizeName(item.name) === normalizeName(pageForm.departmentName)
    )

    if (!matched) {
      pageRows.value = []
      pageTotal.value = 0
      currentUser.value = null
      detailResult.value = null
      roleResult.value = []
      return
    }

    pageForm.departmentId = matched.id
  }

  loading.value = true
  try {
    const payload = await userApi.page({
      page: pageForm.page,
      size: pageForm.size,
      username: pageForm.username,
      realName: pageForm.realName,
      departmentId: pageForm.departmentId,
      status: pageForm.status
    })
    const pageResult = normalizePageResult(payload, [])
    pageRows.value = pageResult.records
    pageTotal.value = pageResult.total
  } catch {
    pageRows.value = []
    pageTotal.value = 0
  } finally {
    loading.value = false
  }
}

async function loadRoleList(id) {
  if (!id) {
    roleResult.value = []
    return
  }

  try {
    const payload = await roleApi.list({ id })
    const data = payload?.data

    if (Array.isArray(data)) {
      roleResult.value = data
      return
    }

    roleResult.value = data ? [data] : []
  } catch {
    roleResult.value = []
  }
}

async function scrollToDetailSection() {
  await nextTick()
  detailSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function loadDetail(id) {
  if (!id) {
    ElMessage.warning('请先选择用户')
    return false
  }

  const payload = await requestWrap(() => userApi.detail({ id }))
  if (!payload?.data) return false

  currentUser.value = payload.data
  detailResult.value = payload.data
  fillEditForm(payload.data)
  resetForm.id = payload.data.id ?? null

  await loadRoleList(id)
  await scrollToDetailSection()
  return true
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

async function openEditDialog(row) {
  const targetId = row?.id || currentUser.value?.id
  if (!targetId) {
    ElMessage.warning('请先选择要修改的用户')
    return
  }

  const loaded = await loadDetail(targetId)
  if (loaded) {
    editDialogVisible.value = true
  }
}

function openResetDialog(row) {
  const targetId = row?.id || currentUser.value?.id
  if (!targetId) {
    ElMessage.warning('请先选择要重置密码的用户')
    return
  }

  resetForm.id = targetId
  resetForm.newPassword = ''
  resetDialogVisible.value = true
}

async function submitCreateUser() {
  if (!createForm.username.trim() || !createForm.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  createSubmitting.value = true
  const payload = await requestWrap(() => userApi.create(createForm), '用户新增成功')
  createSubmitting.value = false
  if (!payload) return

  createDialogVisible.value = false
  resetCreateForm()
  await loadPage()
}

async function submitEditUser() {
  if (!editForm.id) {
    ElMessage.warning('当前没有可修改的用户')
    return
  }

  editSubmitting.value = true
  const payload = await requestWrap(() => userApi.update(editForm), '用户修改成功')
  editSubmitting.value = false
  if (!payload) return

  editDialogVisible.value = false
  await Promise.all([loadPage(), loadDetail(editForm.id)])
}

async function submitResetPassword() {
  if (!resetForm.id || !resetForm.newPassword.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }

  resetSubmitting.value = true
  const payload = await requestWrap(() => userApi.resetPassword(resetForm), '密码重置成功')
  resetSubmitting.value = false
  if (!payload) return

  resetDialogVisible.value = false
  resetResetForm()
}

async function confirmDeleteUser(row) {
  const targetId = row?.id || currentUser.value?.id
  const targetName =
    row?.realName || currentUser.value?.realName || row?.username || currentUser.value?.username || ''

  if (!targetId) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除用户“${targetName || targetId}”吗？删除后将不可恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  const payload = await requestWrap(() => userApi.remove({ id: targetId }), '用户删除成功')
  if (!payload) return

  if (currentUser.value?.id === targetId) {
    currentUser.value = null
    detailResult.value = null
    roleResult.value = []
  }

  resetResetForm()
  resetEditForm()
  await loadPage()
}

function handleCurrentChange(page) {
  pageForm.page = page
  loadPage()
}

function handleSizeChange(size) {
  pageForm.size = size
  pageForm.page = 1
  loadPage()
}

function handleViewDetail(row) {
  loadDetail(row.id)
}

function resetPageQuery() {
  Object.assign(pageForm, {
    page: 1,
    size: pageForm.size,
    username: '',
    realName: '',
    departmentId: null,
    departmentName: '',
    status: ''
  })
  loadPage()
}

onMounted(async () => {
  await loadDepartments()
  await loadPage()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">维护用户资料、角色分配与账号状态。</p>
      </div>
    </div>

    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header-row">
          <span>分页查询</span>
          <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
        </div>
      </template>

      <el-form label-width="100px" class="query-grid">
        <el-form-item label="用户名">
          <el-input v-model="pageForm.username" placeholder="按用户名模糊查询" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="pageForm.realName" placeholder="按姓名模糊查询" />
        </el-form-item>
        <el-form-item label="部门名称">
          <el-autocomplete
            v-model="pageForm.departmentName"
            class="full-width"
            :fetch-suggestions="queryDepartmentSuggestions"
            clearable
            value-key="value"
            placeholder="按部门名称查询"
            @select="handleDepartmentSelect"
            @blur="syncQueryDepartmentByName"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="pageForm.status" placeholder="ACTIVE / DISABLED" />
        </el-form-item>
      </el-form>

      <div class="toolbar-row">
        <el-button type="primary" @click="loadPage">查询用户</el-button>
        <el-button @click="resetPageQuery">重置</el-button>
      </div>

      <el-table :data="pageRows" stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="用户名" prop="username" min-width="130" />
        <el-table-column label="姓名" prop="realName" min-width="120" />
        <el-table-column label="工号" prop="employeeNo" min-width="120" />
        <el-table-column label="部门" prop="departmentName" min-width="150" />
        <el-table-column label="状态" prop="status" width="110" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            {{ formatRoleNames(row) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-action-group">
              <el-button link type="primary" @click="handleViewDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">修改</el-button>
              <el-button link type="warning" @click="openResetDialog(row)">重置密码</el-button>
              <el-button link type="danger" @click="confirmDeleteUser(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && !pageRows.length"
        description="查询结果为空"
        style="margin-top: 16px"
      />

      <div class="pagination-bar">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pageForm.page"
          :page-size="pageForm.size"
          :total="pageTotal"
          :page-sizes="[10, 20, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <div ref="detailSectionRef" class="split-grid" style="margin-top: 20px">
      <el-card shadow="never" class="page-card">
        <template #header><span>用户详情</span></template>
        <el-empty v-if="!detailResult" description="点击上方列表中的“详情”查看用户信息" />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="用户名">{{ detailResult.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detailResult.realName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ detailResult.employeeNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detailResult.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailResult.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailResult.departmentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailResult.status || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>用户角色</span></template>
        <el-empty v-if="!detailResult" description="先选择用户，再查看角色信息" />
        <el-empty v-else-if="!roleResult.length" description="当前用户暂无角色信息" />
        <el-table v-else :data="roleResult" stripe>
          <el-table-column label="角色 ID" prop="id" width="90" />
          <el-table-column label="角色编码" prop="code" min-width="140" />
          <el-table-column label="角色名称" prop="name" min-width="140" />
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="createDialogVisible" title="新增用户" width="560px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="createForm.realName" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="createForm.employeeNo" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="createForm.departmentId" clearable class="full-width" filterable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="`${item.name} (${item.id})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="createForm.status" class="full-width">
            <el-option label="ACTIVE" value="ACTIVE" />
            <el-option label="DISABLED" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.roleIds" multiple class="full-width">
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="`${item.label} (${item.id})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSubmitting" @click="submitCreateUser">
            提交新增
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改用户" width="560px" destroy-on-close>
      <el-form label-width="110px">
        <el-form-item label="用户 ID">
          <el-input :model-value="editForm.id ?? '-'" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="editForm.employeeNo" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="editForm.departmentId" clearable class="full-width" filterable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="`${item.name} (${item.id})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" class="full-width">
            <el-option label="ACTIVE" value="ACTIVE" />
            <el-option label="DISABLED" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleIds" multiple class="full-width">
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="`${item.label} (${item.id})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="submitEditUser">
            提交修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialogVisible" title="重置密码" width="420px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="用户 ID">
          <el-input :model-value="resetForm.id ?? '-'" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="warning" :loading="resetSubmitting" @click="submitResetPassword">
            提交重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.table-action-group {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1200px) {
  .query-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .query-grid {
    grid-template-columns: 1fr;
  }

  .card-header-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
