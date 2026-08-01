<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { departmentApi, userApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'

const loading = ref(false)
const activeTab = ref('tree')

const treeData = ref([])
const allDepartments = ref([])
const userOptions = ref([])
const pageRows = ref([])
const pageTotal = ref(0)
const detailResult = ref(null)
const currentNode = ref(null)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const createSubmitting = ref(false)
const editSubmitting = ref(false)

const pageForm = reactive({
  page: 1,
  size: 10,
  name: ''
})

const createForm = reactive({
  name: '',
  parentId: null,
  parentName: '',
  managerUserId: null,
  managerUserName: '',
  sortOrder: 0,
  remark: ''
})

const editForm = reactive({
  id: null,
  name: '',
  parentId: null,
  parentName: '',
  managerUserId: null,
  managerUserName: '',
  sortOrder: 0,
  remark: ''
})

const userMapById = computed(() => {
  const map = new Map()
  userOptions.value.forEach((item) => {
    if (item?.id != null) {
      map.set(Number(item.id), item)
    }
  })
  return map
})

const departmentMapById = computed(() => {
  const map = new Map()
  allDepartments.value.forEach((item) => {
    if (item?.id != null) {
      map.set(Number(item.id), item)
    }
  })
  return map
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

function enrichDepartmentRow(row = {}) {
  const parent = row.parentId != null ? departmentMapById.value.get(Number(row.parentId)) : null
  const manager = row.managerUserId != null ? userMapById.value.get(Number(row.managerUserId)) : null

  return {
    ...row,
    parentName: row.parentName || parent?.name || '',
    managerUserName: row.managerUserName || manager?.realName || ''
  }
}

function resetCreateForm() {
  Object.assign(createForm, {
    name: '',
    parentId: null,
    parentName: '',
    managerUserId: null,
    managerUserName: '',
    sortOrder: 0,
    remark: ''
  })
}

function resetEditForm() {
  Object.assign(editForm, {
    id: null,
    name: '',
    parentId: null,
    parentName: '',
    managerUserId: null,
    managerUserName: '',
    sortOrder: 0,
    remark: ''
  })
}

function fillEditForm(data = {}) {
  Object.assign(editForm, {
    id: data.id ?? null,
    name: data.name ?? '',
    parentId: data.parentId ?? null,
    parentName: data.parentName ?? '',
    managerUserId: data.managerUserId ?? null,
    managerUserName: data.managerUserName ?? '',
    sortOrder: data.sortOrder ?? 0,
    remark: data.remark ?? ''
  })
}

function buildDepartmentSuggestions(queryString) {
  const keyword = normalizeName(queryString)

  return allDepartments.value
    .filter((item) => !keyword || normalizeName(item.name).includes(keyword))
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function buildUserSuggestions(queryString) {
  const keyword = normalizeName(queryString)

  return userOptions.value
    .filter((item) => {
      const realName = normalizeName(item.realName)
      const username = normalizeName(item.username)
      return !keyword || realName.includes(keyword) || username.includes(keyword)
    })
    .map((item) => ({
      value: item.realName || '',
      id: item.id,
      username: item.username || ''
    }))
}

function queryDepartmentSuggestions(queryString, callback) {
  callback(buildDepartmentSuggestions(queryString))
}

function queryUserSuggestions(queryString, callback) {
  callback(buildUserSuggestions(queryString))
}

function syncDepartmentById(formModel) {
  if (formModel.parentId == null || formModel.parentId === '') {
    formModel.parentName = ''
    return
  }

  const department = departmentMapById.value.get(Number(formModel.parentId))
  formModel.parentName = department?.name || ''
}

function syncDepartmentByName(formModel) {
  const normalized = normalizeName(formModel.parentName)
  if (!normalized) {
    formModel.parentId = null
    return
  }

  const matched = allDepartments.value.find((item) => normalizeName(item.name) === normalized)
  if (matched) {
    formModel.parentId = matched.id
  }
}

function syncManagerById(formModel) {
  if (formModel.managerUserId == null || formModel.managerUserId === '') {
    formModel.managerUserName = ''
    return
  }

  const user = userMapById.value.get(Number(formModel.managerUserId))
  formModel.managerUserName = user?.realName || ''
}

function findUsersByExactName(name) {
  const normalized = normalizeName(name)
  if (!normalized) return []

  return userOptions.value.filter((item) => normalizeName(item.realName) === normalized)
}

function syncManagerByName(formModel) {
  const normalized = normalizeName(formModel.managerUserName)
  if (!normalized) {
    formModel.managerUserId = null
    return
  }

  const matchedUsers = findUsersByExactName(formModel.managerUserName)
  if (matchedUsers.length === 1) {
    formModel.managerUserId = matchedUsers[0].id
    return
  }

  if (matchedUsers.length === 0) {
    formModel.managerUserId = null
  }
}

function handleDepartmentSelect(formModel, item) {
  formModel.parentId = item.id
  formModel.parentName = item.value
}

function handleManagerSelect(formModel, item) {
  formModel.managerUserId = item.id
  formModel.managerUserName = item.value
}

function getManagerIdOptions(formModel) {
  const exactMatches = findUsersByExactName(formModel.managerUserName)
  if (exactMatches.length > 1) {
    return exactMatches
  }

  return userOptions.value.slice(0, 200)
}

async function loadTree() {
  const payload = await requestWrap(() => departmentApi.tree({}))
  const data = payload?.data
  treeData.value = Array.isArray(data) ? data : data ? [data] : []
}

async function loadGetAll() {
  const payload = await requestWrap(() => departmentApi.getAll({}))
  allDepartments.value = Array.isArray(payload?.data) ? payload.data : []
}

async function loadUsers() {
  const payload = await requestWrap(() =>
    userApi.page({
      page: 1,
      size: 1000,
      username: '',
      realName: '',
      departmentId: null,
      status: ''
    })
  )

  userOptions.value = normalizePageResult(payload, []).records
}

async function loadPage() {
  const payload = await requestWrap(() => departmentApi.page(pageForm))
  const pageResult = normalizePageResult(payload, [])
  pageRows.value = pageResult.records.map(enrichDepartmentRow)
  pageTotal.value = pageResult.total
}

async function loadDetail(id) {
  if (!id) {
    ElMessage.warning('请先选择部门节点')
    return
  }

  const payload = await requestWrap(() => departmentApi.detail({ id }))
  if (!payload?.data) return

  const data = enrichDepartmentRow(payload.data)
  detailResult.value = data
  currentNode.value = data
}

function buildDepartmentPayload(formModel) {
  return {
    name: formModel.name,
    parentId: formModel.parentId,
    managerUserId: formModel.managerUserId,
    sortOrder: formModel.sortOrder,
    remark: formModel.remark
  }
}

function openCreateDialog(parentNode = null) {
  resetCreateForm()
  if (parentNode?.id != null) {
    createForm.parentId = parentNode.id
    createForm.parentName = parentNode.name || ''
  }
  createDialogVisible.value = true
}

function openEditDialog() {
  if (!currentNode.value?.id) {
    ElMessage.warning('请先在部门树中选择一个部门节点')
    return
  }

  fillEditForm(currentNode.value)
  editDialogVisible.value = true
}

async function submitCreateDepartment() {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入部门名称')
    return
  }

  createSubmitting.value = true
  const payload = await requestWrap(
    () => departmentApi.create(buildDepartmentPayload(createForm)),
    '部门新增成功'
  )
  createSubmitting.value = false
  if (!payload) return

  createDialogVisible.value = false
  await Promise.all([loadUsers(), loadGetAll()]).then(() => Promise.all([loadTree(), loadPage()]))
}

async function submitEditDepartment() {
  if (!editForm.id) {
    ElMessage.warning('当前没有可修改的部门')
    return
  }

  if (!editForm.name.trim()) {
    ElMessage.warning('请输入部门名称')
    return
  }

  editSubmitting.value = true
  const payload = await requestWrap(
    () =>
      departmentApi.update({
        id: editForm.id,
        ...buildDepartmentPayload(editForm)
      }),
    '部门修改成功'
  )
  editSubmitting.value = false
  if (!payload) return

  editDialogVisible.value = false
  await Promise.all([loadUsers(), loadGetAll()]).then(() =>
    Promise.all([loadTree(), loadPage(), loadDetail(editForm.id)])
  )
}

async function confirmDeleteDepartment() {
  if (!currentNode.value?.id) {
    ElMessage.warning('请先选择要删除的部门节点')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除部门“${currentNode.value.name}”吗？删除后将不可恢复。`,
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

  const deletingId = currentNode.value.id
  const payload = await requestWrap(
    () => departmentApi.remove({ id: deletingId }),
    '部门删除成功'
  )
  if (!payload) return

  currentNode.value = null
  detailResult.value = null
  resetEditForm()
  await Promise.all([loadUsers(), loadGetAll()]).then(() => Promise.all([loadTree(), loadPage()]))
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

function handleTreeNodeClick(node) {
  currentNode.value = enrichDepartmentRow(node)
  loadDetail(node.id)
}

function resetPageQuery() {
  Object.assign(pageForm, {
    page: 1,
    size: pageForm.size,
    name: ''
  })
  loadPage()
}

watch(() => createForm.parentId, () => syncDepartmentById(createForm))
watch(() => createForm.parentName, () => syncDepartmentByName(createForm))
watch(() => createForm.managerUserId, () => syncManagerById(createForm))
watch(() => createForm.managerUserName, () => syncManagerByName(createForm))

watch(() => editForm.parentId, () => syncDepartmentById(editForm))
watch(() => editForm.parentName, () => syncDepartmentByName(editForm))
watch(() => editForm.managerUserId, () => syncManagerById(editForm))
watch(() => editForm.managerUserName, () => syncManagerByName(editForm))

onMounted(() => {
  Promise.all([loadUsers(), loadGetAll()]).then(() => {
    loadTree()
    loadPage()
  })
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">部门管理</h1>
        <p class="page-desc">维护组织架构、负责人和层级关系。</p>
      </div>
      <div class="toolbar-row">
        <el-button type="primary" @click="loadTree">刷新部门树</el-button>
        <el-button @click="loadGetAll">刷新全部列表</el-button>
        <el-button @click="loadPage">刷新分页</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="部门树" name="tree">
        <div class="split-grid">
          <el-card shadow="never" class="page-card">
            <template #header>
              <div class="card-header-row">
                <span>部门树</span>
                <el-button type="primary" plain @click="openCreateDialog()">
                  新增根部门
                </el-button>
              </div>
            </template>
            <el-tree
              v-loading="loading"
              :data="treeData"
              node-key="id"
              default-expand-all
              highlight-current
              :props="{ label: 'name', children: 'children' }"
              @node-click="handleTreeNodeClick"
            />
          </el-card>

          <el-card shadow="never" class="page-card">
            <template #header>
              <div class="card-header-row">
                <span>部门节点信息</span>
                <el-button
                  type="primary"
                  :disabled="!currentNode"
                  @click="openCreateDialog(currentNode)"
                >
                  新增部门
                </el-button>
              </div>
            </template>

            <el-empty v-if="!detailResult" description="点击左侧部门树节点查看详情并进行管理" />

            <template v-else>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="ID">{{ detailResult.id }}</el-descriptions-item>
                <el-descriptions-item label="部门名称">{{ detailResult.name }}</el-descriptions-item>
                <el-descriptions-item label="上级部门 ID">{{ detailResult.parentId ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="上级部门名称">{{ detailResult.parentName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="负责人 ID">{{ detailResult.managerUserId ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="负责人名称">{{ detailResult.managerUserName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="排序">{{ detailResult.sortOrder ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ detailResult.remark || '-' }}</el-descriptions-item>
              </el-descriptions>

              <div class="action-row">
                <el-button type="primary" @click="openEditDialog">修改当前部门</el-button>
                <el-button type="danger" plain @click="confirmDeleteDepartment">删除当前部门</el-button>
              </div>
            </template>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分页列表" name="query">
        <div class="split-grid single-column">
          <el-card shadow="never" class="page-card">
            <template #header><span>部门分页查询</span></template>
            <el-form label-width="100px">
              <el-form-item label="部门名称">
                <el-input v-model="pageForm.name" placeholder="按部门名称模糊查询" />
              </el-form-item>
            </el-form>

            <div class="toolbar-row">
              <el-button type="primary" @click="loadPage">查询</el-button>
              <el-button @click="resetPageQuery">重置</el-button>
            </div>

            <el-table :data="pageRows" stripe v-loading="loading" style="margin-top: 16px">
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="部门名称" prop="name" min-width="160" />
              <el-table-column label="上级部门名称" prop="parentName" min-width="180" />
              <el-table-column label="负责人名称" prop="managerUserName" min-width="160" />
              <el-table-column label="排序" prop="sortOrder" width="100" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="primary" @click="loadDetail(row.id); activeTab = 'tree'">定位</el-button>
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
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="createDialogVisible" title="新增部门" width="560px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="部门名称">
          <el-input v-model="createForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门 ID">
          <el-input-number v-model="createForm.parentId" class="full-width" />
        </el-form-item>
        <el-form-item label="上级部门名称">
          <el-autocomplete
            v-model="createForm.parentName"
            class="full-width"
            :fetch-suggestions="queryDepartmentSuggestions"
            clearable
            value-key="value"
            placeholder="输入或选择上级部门名称"
            @select="handleDepartmentSelect(createForm, $event)"
          />
        </el-form-item>
        <el-form-item label="负责人 ID">
          <el-select
            v-model="createForm.managerUserId"
            class="full-width"
            clearable
            filterable
            placeholder="输入或选择负责人 ID"
          >
            <el-option
              v-for="user in getManagerIdOptions(createForm)"
              :key="user.id"
              :label="`${user.id} - ${user.realName || user.username}`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人名称">
          <el-autocomplete
            v-model="createForm.managerUserName"
            class="full-width"
            :fetch-suggestions="queryUserSuggestions"
            clearable
            value-key="value"
            placeholder="输入负责人名称后自动联动 ID"
            @select="handleManagerSelect(createForm, $event)"
          >
            <template #default="{ item }">
              <div class="suggestion-row">
                <span>{{ item.value }}</span>
                <span class="suggestion-meta">ID: {{ item.id }}</span>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="createForm.sortOrder" class="full-width" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可选，填写部门说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSubmitting" @click="submitCreateDepartment">
            提交新增
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改部门" width="560px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="部门 ID">
          <el-input :model-value="editForm.id ?? '-'" disabled />
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="editForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门 ID">
          <el-input-number v-model="editForm.parentId" class="full-width" />
        </el-form-item>
        <el-form-item label="上级部门名称">
          <el-autocomplete
            v-model="editForm.parentName"
            class="full-width"
            :fetch-suggestions="queryDepartmentSuggestions"
            clearable
            value-key="value"
            placeholder="输入或选择上级部门名称"
            @select="handleDepartmentSelect(editForm, $event)"
          />
        </el-form-item>
        <el-form-item label="负责人 ID">
          <el-select
            v-model="editForm.managerUserId"
            class="full-width"
            clearable
            filterable
            placeholder="输入或选择负责人 ID"
          >
            <el-option
              v-for="user in getManagerIdOptions(editForm)"
              :key="user.id"
              :label="`${user.id} - ${user.realName || user.username}`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人名称">
          <el-autocomplete
            v-model="editForm.managerUserName"
            class="full-width"
            :fetch-suggestions="queryUserSuggestions"
            clearable
            value-key="value"
            placeholder="输入负责人名称后自动联动 ID"
            @select="handleManagerSelect(editForm, $event)"
          >
            <template #default="{ item }">
              <div class="suggestion-row">
                <span>{{ item.value }}</span>
                <span class="suggestion-meta">ID: {{ item.id }}</span>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" class="full-width" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可选，填写部门说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="submitEditDepartment">
            提交修改
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

.single-column {
  grid-template-columns: 1fr;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.suggestion-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.suggestion-meta {
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 768px) {
  .card-header-row,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
