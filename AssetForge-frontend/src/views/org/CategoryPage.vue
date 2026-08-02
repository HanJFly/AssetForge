<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { categoryApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'

const loading = ref(false)
const activeTab = ref('tree')

const treeData = ref([])
const allCategories = ref([])
const pageRows = ref([])
const pageTotal = ref(0)
const currentNode = ref(null)
const detailResult = ref(null)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const createSubmitting = ref(false)
const editSubmitting = ref(false)

const categoryMapById = computed(() => {
  const map = new Map()
  allCategories.value.forEach((item) => {
    if (item?.id != null) {
      map.set(Number(item.id), item)
    }
  })
  return map
})

const pageForm = reactive({
  page: 1,
  size: 10,
  name: '',
  parentId: null,
  parentName: ''
})

const createForm = reactive({
  name: '',
  parentId: null,
  parentName: '',
  standardLifeMonths: null,
  sortOrder: 0
})

const editForm = reactive({
  id: null,
  name: '',
  parentId: null,
  parentName: '',
  standardLifeMonths: null,
  sortOrder: 0
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

function enrichCategoryRow(row = {}) {
  const parent = row.parentId != null ? categoryMapById.value.get(Number(row.parentId)) : null
  return {
    ...row,
    parentName: row.parentName || parent?.name || ''
  }
}

function buildCategorySuggestions(queryString) {
  const keyword = normalizeName(queryString)
  return allCategories.value
    .filter((item) => !keyword || normalizeName(item.name).includes(keyword))
    .map((item) => ({
      value: item.name,
      id: item.id
    }))
}

function queryCategorySuggestions(queryString, callback) {
  callback(buildCategorySuggestions(queryString))
}

function syncCategoryById(formModel) {
  if (formModel.parentId == null || formModel.parentId === '') {
    formModel.parentName = ''
    return
  }

  const category = categoryMapById.value.get(Number(formModel.parentId))
  formModel.parentName = category?.name || ''
}

function syncCategoryByName(formModel) {
  const normalized = normalizeName(formModel.parentName)
  if (!normalized) {
    formModel.parentId = null
    return
  }

  const matched = allCategories.value.find((item) => normalizeName(item.name) === normalized)
  if (matched) {
    formModel.parentId = matched.id
  }
}

function handleCategorySelect(formModel, item) {
  formModel.parentId = item.id
  formModel.parentName = item.value
}

function resetCreateForm() {
  Object.assign(createForm, {
    name: '',
    parentId: null,
    parentName: '',
    standardLifeMonths: null,
    sortOrder: 0
  })
}

function resetEditForm() {
  Object.assign(editForm, {
    id: null,
    name: '',
    parentId: null,
    parentName: '',
    standardLifeMonths: null,
    sortOrder: 0
  })
}

function fillEditForm(data = {}) {
  Object.assign(editForm, {
    id: data.id ?? null,
    name: data.name ?? '',
    parentId: data.parentId ?? null,
    parentName: data.parentName ?? '',
    standardLifeMonths: data.standardLifeMonths ?? null,
    sortOrder: data.sortOrder ?? 0
  })
}

async function loadTree() {
  const payload = await requestWrap(() => categoryApi.tree({}))
  const data = payload?.data
  treeData.value = Array.isArray(data) ? data : data ? [data] : []
}

async function loadAllCategories() {
  const payload = await requestWrap(() => categoryApi.page({ page: 1, size: 1000, name: '', parentId: null }))
  allCategories.value = normalizePageResult(payload, []).records
}

async function loadPage() {
  const nameKeyword = normalizeName(pageForm.name)
  const parentKeyword = normalizeName(pageForm.parentName)

  const filteredRows = allCategories.value
    .map(enrichCategoryRow)
    .filter((item) => {
      const matchedName = !nameKeyword || normalizeName(item.name).includes(nameKeyword)
      const matchedParent = !parentKeyword || normalizeName(item.parentName).includes(parentKeyword)
      return matchedName && matchedParent
    })

  pageTotal.value = filteredRows.length

  const start = (pageForm.page - 1) * pageForm.size
  const end = start + pageForm.size
  pageRows.value = filteredRows.slice(start, end)
}

async function loadDetail(id) {
  if (!id) {
    ElMessage.warning('请先选择分类节点')
    return
  }

  const payload = await requestWrap(() => categoryApi.detail({ id }))
  if (!payload?.data) return

  const data = enrichCategoryRow(payload.data)
  detailResult.value = data
  currentNode.value = data
}

function buildCategoryPayload(formModel) {
  return {
    name: formModel.name,
    parentId: formModel.parentId,
    standardLifeMonths: formModel.standardLifeMonths,
    sortOrder: formModel.sortOrder
  }
}

function validateCategoryForm(formModel) {
  if (!formModel.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return false
  }

  const isChildCategory = formModel.parentId != null && formModel.parentId !== '' && Number(formModel.parentId) !== 0
  if (isChildCategory && (formModel.standardLifeMonths == null || Number(formModel.standardLifeMonths) <= 0)) {
    ElMessage.warning('子分类必须填写标准使用年限，且必须大于 0')
    return false
  }

  return true
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
    ElMessage.warning('请先在分类树中选择一个分类节点')
    return
  }

  fillEditForm(currentNode.value)
  editDialogVisible.value = true
}

async function submitCreateCategory() {
  if (!validateCategoryForm(createForm)) {
    return
  }

  createSubmitting.value = true
  const payload = await requestWrap(
    () => categoryApi.create(buildCategoryPayload(createForm)),
    '分类新增成功'
  )
  createSubmitting.value = false
  if (!payload) return

  createDialogVisible.value = false
  await Promise.all([loadAllCategories(), loadTree(), loadPage()])
}

async function submitEditCategory() {
  if (!editForm.id) {
    ElMessage.warning('当前没有可修改的分类')
    return
  }

  if (!validateCategoryForm(editForm)) {
    return
  }

  editSubmitting.value = true
  const payload = await requestWrap(
    () =>
      categoryApi.update({
        id: editForm.id,
        ...buildCategoryPayload(editForm)
      }),
    '分类修改成功'
  )
  editSubmitting.value = false
  if (!payload) return

  editDialogVisible.value = false
  await Promise.all([loadAllCategories(), loadTree(), loadPage(), loadDetail(editForm.id)])
}

async function confirmDeleteCategory() {
  if (!currentNode.value?.id) {
    ElMessage.warning('请先选择要删除的分类节点')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除分类“${currentNode.value.name}”吗？删除后将不可恢复。`,
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
    () => categoryApi.remove({ id: deletingId }),
    '分类删除成功'
  )
  if (!payload) return

  currentNode.value = null
  detailResult.value = null
  resetEditForm()
  await Promise.all([loadAllCategories(), loadTree(), loadPage()])
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

function handleNodeClick(node) {
  currentNode.value = enrichCategoryRow(node)
  loadDetail(node.id)
}

function resetPageQuery() {
  Object.assign(pageForm, {
    page: 1,
    size: pageForm.size,
    name: '',
    parentId: null,
    parentName: ''
  })
  loadPage()
}

watch(() => createForm.parentId, () => syncCategoryById(createForm))
watch(() => createForm.parentName, () => syncCategoryByName(createForm))
watch(() => editForm.parentId, () => syncCategoryById(editForm))
watch(() => editForm.parentName, () => syncCategoryByName(editForm))

onMounted(() => {
  loadAllCategories().then(() => {
    loadTree()
    loadPage()
  })
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">资产分类</h1>
        <p class="page-desc">维护资产分类层级与折旧年限。</p>
      </div>
      <div class="toolbar-row">
        <el-button type="primary" @click="loadTree">刷新分类树</el-button>
        <el-button @click="loadPage">刷新分页</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="分类树" name="tree">
        <div class="split-grid">
          <el-card shadow="never" class="page-card">
            <template #header>
              <div class="card-header-row">
                <span>分类树</span>
                <el-button type="primary" plain @click="openCreateDialog()">新增根分类</el-button>
              </div>
            </template>
            <el-tree
              v-loading="loading"
              class="management-tree"
              :data="treeData"
              node-key="id"
              default-expand-all
              highlight-current
              :props="{ label: 'name', children: 'children' }"
              @node-click="handleNodeClick"
            />
          </el-card>

          <el-card shadow="never" class="page-card">
            <template #header>
              <div class="card-header-row">
                <span>分类节点信息</span>
                <el-button
                  type="primary"
                  :disabled="!currentNode"
                  @click="openCreateDialog(currentNode)"
                >
                  新增分类
                </el-button>
              </div>
            </template>

            <el-empty v-if="!detailResult" description="点击左侧分类树节点查看详情并进行管理" />

            <template v-else>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="ID">{{ detailResult.id }}</el-descriptions-item>
                <el-descriptions-item label="分类名称">{{ detailResult.name }}</el-descriptions-item>
                <el-descriptions-item label="上级分类 ID">{{ detailResult.parentId ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="上级分类名称">{{ detailResult.parentName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="标准使用年限(月)">
                  {{ detailResult.standardLifeMonths ?? '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="排序">{{ detailResult.sortOrder ?? '-' }}</el-descriptions-item>
              </el-descriptions>

              <div class="action-row">
                <el-button type="primary" @click="openEditDialog">修改当前分类</el-button>
                <el-button type="danger" plain @click="confirmDeleteCategory">删除当前分类</el-button>
              </div>
            </template>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分页列表" name="query">
        <div class="split-grid single-column">
          <el-card shadow="never" class="page-card">
            <template #header><span>分类分页查询</span></template>
            <el-form label-width="120px" class="query-grid">
              <el-form-item label="分类名称">
                <el-input v-model="pageForm.name" placeholder="按分类名称模糊查询" />
              </el-form-item>
              <el-form-item label="上级分类名称">
                <el-autocomplete
                  v-model="pageForm.parentName"
                  class="full-width"
                  :fetch-suggestions="queryCategorySuggestions"
                  clearable
                  value-key="value"
                  placeholder="输入上级分类名称后自动联动 ID"
                  @select="handleCategorySelect(pageForm, $event)"
                />
              </el-form-item>
            </el-form>

            <div class="toolbar-row">
              <el-button type="primary" @click="loadPage">查询</el-button>
              <el-button @click="resetPageQuery">重置</el-button>
            </div>

            <el-table :data="pageRows" stripe v-loading="loading" style="margin-top: 16px">
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column label="分类名称" prop="name" min-width="180" />
              <el-table-column label="上级分类名称" prop="parentName" min-width="180" />
              <el-table-column label="标准使用年限(月)" prop="standardLifeMonths" width="140" />
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

    <el-dialog v-model="createDialogVisible" title="新增分类" width="520px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="分类名称">
          <el-input v-model="createForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类 ID">
          <el-input-number v-model="createForm.parentId" class="full-width" />
        </el-form-item>
        <el-form-item label="上级分类名称">
          <el-autocomplete
            v-model="createForm.parentName"
            class="full-width"
            :fetch-suggestions="queryCategorySuggestions"
            clearable
            value-key="value"
            placeholder="输入或选择上级分类名称"
            @select="handleCategorySelect(createForm, $event)"
          />
        </el-form-item>
        <el-form-item label="标准使用年限(月)">
          <el-input-number v-model="createForm.standardLifeMonths" class="full-width" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="createForm.sortOrder" class="full-width" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSubmitting" @click="submitCreateCategory">
            提交新增
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改分类" width="520px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="分类 ID">
          <el-input :model-value="editForm.id ?? '-'" disabled />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类 ID">
          <el-input-number v-model="editForm.parentId" class="full-width" />
        </el-form-item>
        <el-form-item label="上级分类名称">
          <el-autocomplete
            v-model="editForm.parentName"
            class="full-width"
            :fetch-suggestions="queryCategorySuggestions"
            clearable
            value-key="value"
            placeholder="输入或选择上级分类名称"
            @select="handleCategorySelect(editForm, $event)"
          />
        </el-form-item>
        <el-form-item label="标准使用年限(月)">
          <el-input-number v-model="editForm.standardLifeMonths" class="full-width" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" class="full-width" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editSubmitting" @click="submitEditCategory">
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

.query-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
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

:deep(.management-tree .el-tree-node__content) {
  min-height: 38px;
  font-size: 17px;
}

:deep(.management-tree .el-tree-node__label) {
  font-size: 17px;
  line-height: 1.5;
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

  .card-header-row,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
