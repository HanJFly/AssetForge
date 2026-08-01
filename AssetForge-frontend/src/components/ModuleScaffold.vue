<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { normalizeDataResult, normalizePageResult } from '@/api/helpers'

const props = defineProps({
  title: String,
  description: String,
  note: {
    type: String,
    default: ''
  },
  filters: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  formFields: {
    type: Array,
    default: () => []
  },
  detailFields: {
    type: Array,
    default: () => []
  },
  api: {
    type: Object,
    default: () => ({})
  },
  submitPayloadBuilder: {
    type: Function,
    default: null
  },
  queryPayloadBuilder: {
    type: Function,
    default: null
  },
  mockRecords: {
    type: Array,
    default: () => []
  },
  mockDetail: {
    type: Object,
    default: () => ({})
  },
  permissions: {
    type: Object,
    default: () => ({})
  }
})

const loading = ref(false)
const detailVisible = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const tableData = ref([])
const detailModel = ref({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const queryModel = reactive(buildModel(props.filters, { page: 1, size: 10 }))
const formModel = reactive(buildModel(props.formFields))

const hasCreate = computed(() => props.permissions.create !== false && typeof props.api.create === 'function')
const hasEdit = computed(() => props.permissions.edit !== false && typeof props.api.update === 'function')
const hasRemove = computed(() => props.permissions.remove !== false && typeof props.api.remove === 'function')
const hasDetail = computed(() => props.permissions.detail !== false)
const hasAnyRowAction = computed(() => hasDetail.value || hasEdit.value || hasRemove.value)
const visibleFilters = computed(() => props.filters.filter((field) => field.hidden !== true))
const visibleFormFields = computed(() => props.formFields.filter((field) => field.hidden !== true))

const detailEntries = computed(() => {
  if (props.detailFields.length) {
    return props.detailFields.map((field) => ({
      label: field.label,
      value: detailModel.value?.[field.prop]
    }))
  }

  return Object.entries(detailModel.value || {}).map(([label, value]) => ({ label, value }))
})

function resolveFieldDefault(field) {
  return typeof field.default === 'function' ? field.default() : field.default
}

function buildModel(fields, seed = {}) {
  return fields.reduce((acc, field) => {
    acc[field.prop] = resolveFieldDefault(field) ?? null
    return acc
  }, { ...seed })
}

function resetFormModel(source = {}) {
  props.formFields.forEach((field) => {
    formModel[field.prop] = source[field.prop] ?? resolveFieldDefault(field) ?? null
  })
}

function buildSubmitPayload(mode) {
  if (props.submitPayloadBuilder) {
    return props.submitPayloadBuilder({
      mode,
      formModel: { ...formModel }
    })
  }

  return { ...formModel }
}

function buildQueryPayload() {
  if (props.queryPayloadBuilder) {
    return props.queryPayloadBuilder({
      queryModel: { ...queryModel },
      pagination: { ...pagination }
    })
  }

  return {
    ...queryModel,
    page: pagination.page,
    size: pagination.size
  }
}

function fieldComponent(field) {
  if (field.type === 'select') return 'el-select'
  if (field.type === 'autocomplete') return 'el-autocomplete'
  if (field.type === 'upload') return 'el-upload'
  if (field.type === 'textarea') return 'el-input'
  if (field.type === 'number') return 'el-input-number'
  if (field.type === 'date') return 'el-date-picker'
  return 'el-input'
}

function inputProps(field) {
  if (field.type === 'textarea') {
    return { type: 'textarea', rows: 3, placeholder: field.placeholder, ...(field.componentProps || {}) }
  }

  if (field.type === 'date') {
    return {
      type: 'date',
      valueFormat: 'YYYY-MM-DD',
      placeholder: field.placeholder,
      ...(field.componentProps || {})
    }
  }

  if (field.type === 'upload') {
    return { ...(field.componentProps || {}) }
  }

  return { placeholder: field.placeholder, ...(field.componentProps || {}) }
}

async function fetchList() {
  loading.value = true
  try {
    const payload = props.api.page ? await props.api.page(buildQueryPayload()) : { data: { records: [], total: 0 } }
    const result = normalizePageResult(payload, [])
    tableData.value = result.records
    pagination.total = result.total
    pagination.page = result.page || pagination.page
    pagination.size = result.size || pagination.size
  } catch (error) {
    tableData.value = []
    pagination.total = 0
    ElMessage.error(error?.msg || error?.response?.data?.msg || error?.message || '列表加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.page = 1
  fetchList()
}

function onPageChange(page) {
  pagination.page = page
  fetchList()
}

function onPageSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

function onReset() {
  props.filters.forEach((field) => {
    queryModel[field.prop] = field.default ?? null
  })
  onSearch()
}

function openCreate() {
  dialogMode.value = 'create'
  resetFormModel()
  dialogVisible.value = true
}

async function openEdit(row) {
  dialogMode.value = 'edit'
  try {
    const payload = props.api.detail ? await props.api.detail({ id: row.id }) : { data: row }
    resetFormModel(normalizeDataResult(payload, row) || row)
  } catch {
    resetFormModel(row)
  }
  dialogVisible.value = true
}

async function openDetail(row) {
  try {
    const payload = props.api.detail ? await props.api.detail({ id: row.id }) : { data: row }
    detailModel.value = normalizeDataResult(payload, row) || row
  } catch {
    detailModel.value = row
  }
  detailVisible.value = true
}

async function submitForm() {
  try {
    const payload = buildSubmitPayload(dialogMode.value)

    if (dialogMode.value === 'create') {
      await props.api.create?.(payload)
      ElMessage.success('新增成功')
    } else {
      await props.api.update?.(payload)
      ElMessage.success('修改成功')
    }

    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(error?.msg || error?.response?.data?.msg || error?.message || '保存失败')
  }
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确认删除“${row.name || row.orderNo || row.id}”吗？`, '删除确认', {
      type: 'warning'
    })
    await props.api.remove?.({ id: row.id })
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    return
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
      </div>
      <el-button v-if="hasCreate" type="primary" @click="openCreate">新建</el-button>
    </div>

    <el-card shadow="never" class="page-card">
      <el-form label-width="90px" inline>
        <el-form-item v-for="filter in visibleFilters" :key="filter.prop" :label="filter.label">
          <component
            :is="fieldComponent(filter)"
            v-model="queryModel[filter.prop]"
            v-bind="inputProps(filter)"
            clearable
            style="width: 220px"
          >
            <template v-if="filter.type === 'select'">
              <el-option
                v-for="option in filter.options || []"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </template>
          </component>
        </el-form-item>
      </el-form>

      <div class="toolbar-row">
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="page-card">
      <el-table :data="tableData" v-loading="loading" stripe class="full-width">
        <el-table-column v-for="column in columns" :key="column.prop" v-bind="column" />
        <el-table-column v-if="hasAnyRowAction" label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasDetail" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="hasEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="hasRemove" link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="onPageChange"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? `新建${title}` : `编辑${title}`"
      width="720px"
    >
      <el-form label-width="110px">
        <el-form-item v-for="field in visibleFormFields" :key="field.prop" :label="field.label">
          <el-upload
            v-if="field.type === 'upload'"
            v-model:file-list="formModel[field.prop]"
            v-bind="inputProps(field)"
            style="width: 100%"
          >
            <el-button type="primary">{{ field.uploadButtonText || '上传文件' }}</el-button>
            <template v-if="field.uploadTip" #tip>
              <div class="el-upload__tip">{{ field.uploadTip }}</div>
            </template>
          </el-upload>
          <component
            v-else
            :is="fieldComponent(field)"
            v-model="formModel[field.prop]"
            v-bind="inputProps(field)"
            clearable
            style="width: 100%"
          >
            <template v-if="field.type === 'select'">
              <el-option
                v-for="option in field.options || []"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </template>
          </component>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="详情" size="460px">
      <el-descriptions :column="1" border>
        <el-descriptions-item
          v-for="item in detailEntries"
          :key="item.label"
          :label="item.label"
        >
          {{ item.value ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>
