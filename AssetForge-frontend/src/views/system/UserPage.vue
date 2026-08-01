<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { departmentApi, roleApi, userApi } from '@/api'

const loading = ref(false)
const activeTab = ref('page')

const pageRows = ref([])
const pageTotal = ref(0)
const detailResult = ref(null)
const roleResult = ref(null)
const departmentOptions = ref([])

const roleOptions = [
  { id: 1, label: '普通员工' },
  { id: 2, label: '仓管员' },
  { id: 3, label: '资产管理员' },
  { id: 4, label: '部门管理员' }
]

const pageForm = reactive({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  departmentId: null,
  status: ''
})

const detailForm = reactive({
  id: null
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

const updateForm = reactive({
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

const deleteForm = reactive({
  id: null
})

function normalizePageData(data) {
  const records = data?.records ?? data?.result ?? data?.list ?? []
  const total = data?.total ?? records?.length ?? 0
  return {
    records: Array.isArray(records) ? records : [],
    total
  }
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

async function requestWrap(action, successMessage = '') {
  loading.value = true
  try {
    const payload = await action()
    if (successMessage) {
      ElMessage.success(successMessage)
    }
    return payload
  } catch (error) {
    ElMessage.error(error?.message || '接口调用失败，请检查后端返回')
    return null
  } finally {
    loading.value = false
  }
}

async function loadDepartments() {
  const payload = await requestWrap(() => departmentApi.getAll({}))
  departmentOptions.value = Array.isArray(payload?.data) ? payload.data : []
}

async function loadPage() {
  const payload = await requestWrap(() => userApi.page(pageForm))
  const { records, total } = normalizePageData(payload?.data)
  pageRows.value = records
  pageTotal.value = total
}

async function loadDetail(id = detailForm.id) {
  if (!id) {
    ElMessage.warning('请输入用户 ID')
    return
  }
  detailForm.id = id
  const payload = await requestWrap(() => userApi.detail({ id }))
  if (!payload?.data) return

  detailResult.value = payload.data
  Object.assign(updateForm, {
    id: payload.data.id ?? null,
    username: payload.data.username ?? '',
    realName: payload.data.realName ?? '',
    employeeNo: payload.data.employeeNo ?? '',
    phone: payload.data.phone ?? '',
    email: payload.data.email ?? '',
    departmentId: payload.data.departmentId ?? null,
    status: payload.data.status ?? 'ACTIVE',
    roleIds: []
  })
  resetForm.id = payload.data.id ?? null
  deleteForm.id = payload.data.id ?? null
  roleResult.value = null
}

async function loadRoleList() {
  if (!detailForm.id) {
    ElMessage.warning('请先输入用户 ID')
    return
  }
  const payload = await requestWrap(() => roleApi.list({ id: detailForm.id }))
  if (!payload?.data) return
  roleResult.value = payload.data
}

async function createUser() {
  if (!createForm.username.trim() || !createForm.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  const payload = await requestWrap(() => userApi.create(createForm), '用户新增成功')
  if (!payload) return
  resetCreateForm()
  await loadPage()
}

async function updateUser() {
  if (!updateForm.id) {
    ElMessage.warning('请先查询用户详情')
    return
  }
  const payload = await requestWrap(() => userApi.update(updateForm), '用户修改成功')
  if (!payload) return
  await Promise.all([loadPage(), loadDetail(updateForm.id)])
}

async function resetPassword() {
  if (!resetForm.id || !resetForm.newPassword.trim()) {
    ElMessage.warning('请输入用户 ID 和新密码')
    return
  }
  await requestWrap(() => userApi.resetPassword(resetForm), '密码重置成功')
}

async function deleteUser() {
  if (!deleteForm.id) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }
  const payload = await requestWrap(() => userApi.remove({ id: deleteForm.id }), '用户删除成功')
  if (!payload) return
  detailResult.value = null
  roleResult.value = null
  detailForm.id = null
  deleteForm.id = null
  resetForm.id = null
  resetForm.newPassword = ''
  Object.assign(updateForm, {
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

onMounted(() => {
  loadDepartments()
  loadPage()
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

    <el-tabs v-model="activeTab">
      <el-tab-pane label="分页查询" name="page">
        <el-card shadow="never" class="page-card">
          <el-form label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="pageForm.username" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="pageForm.realName" />
            </el-form-item>
            <el-form-item label="部门 ID">
              <el-input-number v-model="pageForm.departmentId" class="full-width" />
            </el-form-item>
            <el-form-item label="状态">
              <el-input v-model="pageForm.status" placeholder="ACTIVE / DISABLED" />
            </el-form-item>
          </el-form>
          <div class="toolbar-row">
            <el-button type="primary" @click="loadPage">查询用户</el-button>
            <el-button @click="pageForm.username=''; pageForm.realName=''; pageForm.departmentId=null; pageForm.status=''; pageForm.page=1; loadPage()">重置</el-button>
          </div>

          <el-table :data="pageRows" stripe v-loading="loading" style="margin-top: 16px">
            <el-table-column label="ID" prop="id" width="70" />
            <el-table-column label="用户名" prop="username" min-width="120" />
            <el-table-column label="姓名" prop="realName" min-width="120" />
            <el-table-column label="工号" prop="employeeNo" min-width="120" />
            <el-table-column label="部门" prop="departmentName" min-width="140" />
            <el-table-column label="状态" prop="status" width="100" />
            <el-table-column label="角色" min-width="180">
              <template #default="{ row }">
                {{ Array.isArray(row.roleNames) ? row.roleNames.join('、') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="loadDetail(row.id)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

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
      </el-tab-pane>

      <el-tab-pane label="详情与角色" name="detail">
        <div class="split-grid">
          <el-card shadow="never" class="page-card">
            <template #header><span>用户详情</span></template>
            <el-form label-width="90px">
              <el-form-item label="用户 ID">
                <el-input-number v-model="detailForm.id" class="full-width" />
              </el-form-item>
            </el-form>
            <div class="toolbar-row">
              <el-button type="primary" @click="loadDetail()">查询详情</el-button>
              <el-button @click="loadRoleList">查询角色</el-button>
            </div>

            <el-empty v-if="!detailResult" description="请输入用户 ID 或点击列表中的详情" style="margin-top: 16px" />
            <el-descriptions v-else :column="1" border style="margin-top: 16px">
              <el-descriptions-item label="用户名">{{ detailResult.username }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ detailResult.realName }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ detailResult.employeeNo }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ detailResult.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ detailResult.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ detailResult.departmentName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ detailResult.status }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never" class="page-card">
            <template #header><span>用户角色</span></template>
            <el-empty v-if="!roleResult" description="点击“查询角色”后展示结果" />
            <el-descriptions v-else :column="1" border>
              <el-descriptions-item label="角色 ID">{{ roleResult.id }}</el-descriptions-item>
              <el-descriptions-item label="角色编码">{{ roleResult.code }}</el-descriptions-item>
              <el-descriptions-item label="角色名称">{{ roleResult.name }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="新增与修改" name="save">
        <div class="split-grid">
          <el-card shadow="never" class="page-card">
            <template #header><span>新建用户</span></template>
            <el-form label-width="110px">
              <el-form-item label="用户名"><el-input v-model="createForm.username" /></el-form-item>
              <el-form-item label="密码"><el-input v-model="createForm.password" show-password /></el-form-item>
              <el-form-item label="姓名"><el-input v-model="createForm.realName" /></el-form-item>
              <el-form-item label="工号"><el-input v-model="createForm.employeeNo" /></el-form-item>
              <el-form-item label="手机号"><el-input v-model="createForm.phone" /></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="createForm.email" /></el-form-item>
              <el-form-item label="部门 ID">
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
              <el-form-item label="角色 ID 列表">
                <el-select v-model="createForm.roleIds" multiple class="full-width">
                  <el-option v-for="item in roleOptions" :key="item.id" :label="`${item.label} (${item.id})`" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-form>
            <el-button type="primary" @click="createUser">提交新增</el-button>
          </el-card>

          <el-card shadow="never" class="page-card">
            <template #header><span>修改用户</span></template>
            <el-form label-width="110px">
              <el-form-item label="用户 ID">
                <el-input-number v-model="updateForm.id" class="full-width" />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="updateForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名"><el-input v-model="updateForm.realName" /></el-form-item>
              <el-form-item label="工号"><el-input v-model="updateForm.employeeNo" /></el-form-item>
              <el-form-item label="手机号"><el-input v-model="updateForm.phone" /></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="updateForm.email" /></el-form-item>
              <el-form-item label="部门 ID">
                <el-select v-model="updateForm.departmentId" clearable class="full-width" filterable>
                  <el-option
                    v-for="item in departmentOptions"
                    :key="item.id"
                    :label="`${item.name} (${item.id})`"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="updateForm.status" class="full-width">
                  <el-option label="ACTIVE" value="ACTIVE" />
                  <el-option label="DISABLED" value="DISABLED" />
                </el-select>
              </el-form-item>
              <el-form-item label="角色 ID 列表">
                <el-select v-model="updateForm.roleIds" multiple class="full-width">
                  <el-option v-for="item in roleOptions" :key="item.id" :label="`${item.label} (${item.id})`" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-form>
            <el-button type="primary" @click="updateUser">提交修改</el-button>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="重置密码与删除" name="danger">
        <div class="split-grid">
          <el-card shadow="never" class="page-card">
            <template #header><span>重置密码</span></template>
            <el-form label-width="100px">
              <el-form-item label="用户 ID">
                <el-input-number v-model="resetForm.id" class="full-width" />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="resetForm.newPassword" show-password />
              </el-form-item>
            </el-form>
            <el-button type="warning" @click="resetPassword">提交重置</el-button>
          </el-card>

          <el-card shadow="never" class="page-card">
            <template #header><span>删除用户</span></template>
            <el-form label-width="100px">
              <el-form-item label="用户 ID">
                <el-input-number v-model="deleteForm.id" class="full-width" />
              </el-form-item>
            </el-form>
            <el-button type="danger" @click="deleteUser">提交删除</el-button>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
