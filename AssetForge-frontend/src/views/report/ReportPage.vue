<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import StatCard from '@/components/StatCard.vue'
import { reportApi } from '@/api'
import { normalizePageResult } from '@/api/helpers'

const loading = ref(false)
const monthForm = reactive({
  snapshotMonth: '2026-07',
  runMonth: '2026-07'
})

const summary = reactive({
  assetCount: 0,
  originalAmountTotal: 0,
  monthlyDepreciationTotal: 0,
  accumulatedDepreciationTotal: 0,
  netAmountTotal: 0
})

const categorySummaryList = ref([])
const detailList = ref([])
const depreciationList = ref([])

async function loadReport() {
  loading.value = true
  try {
    const [assetDetail, monthlySummary, depreciationPage] = await Promise.all([
      reportApi.assetDetail({
        page: 1,
        size: 10,
        categoryId: null,
        departmentId: null,
        status: null
      }),
      reportApi.monthlySummary({ snapshotMonth: monthForm.snapshotMonth }),
      reportApi.depreciationPage({ page: 1, size: 10, runMonth: monthForm.runMonth })
    ])

    detailList.value = normalizePageResult(assetDetail, []).records
    Object.assign(summary, {
      assetCount: 0,
      originalAmountTotal: 0,
      monthlyDepreciationTotal: 0,
      accumulatedDepreciationTotal: 0,
      netAmountTotal: 0,
      ...(monthlySummary?.data || {})
    })
    categorySummaryList.value = monthlySummary?.data?.categorySummary || []
    depreciationList.value = normalizePageResult(depreciationPage, []).records
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '报表数据加载失败')
  } finally {
    loading.value = false
  }
}

async function runDepreciation() {
  try {
    await reportApi.executeDepreciation({ runMonth: monthForm.runMonth })
    ElMessage.success('已触发折旧执行')
    loadReport()
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '折旧执行失败')
  }
}

loadReport()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">报表与折旧</h1>
        <p class="page-desc">查看资产报表、分类汇总与折旧执行记录。</p>
      </div>
      <div class="toolbar-row">
        <el-date-picker
          v-model="monthForm.snapshotMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择统计月份"
        />
        <el-date-picker
          v-model="monthForm.runMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择折旧月份"
        />
        <el-button @click="loadReport">刷新报表</el-button>
        <el-button type="primary" @click="runDepreciation">执行折旧</el-button>
      </div>
    </div>

    <div class="stats-grid">
      <StatCard title="资产总数" :value="summary.assetCount" desc="月度快照统计" accent="#2563eb" />
      <StatCard title="原值总额" :value="summary.originalAmountTotal" desc="单位：元" accent="#0284c7" />
      <StatCard title="当月折旧" :value="summary.monthlyDepreciationTotal" desc="单位：元" accent="#059669" />
      <StatCard title="累计折旧" :value="summary.accumulatedDepreciationTotal" desc="单位：元" accent="#0f766e" />
      <StatCard title="净值总额" :value="summary.netAmountTotal" desc="单位：元" accent="#ea580c" />
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header><span>资产明细报表</span></template>
        <el-table :data="detailList" stripe v-loading="loading">
          <el-table-column label="资产编码" prop="assetCode" min-width="150" />
          <el-table-column label="资产名称" prop="assetName" min-width="180" />
          <el-table-column label="部门" prop="departmentName" min-width="120" />
          <el-table-column label="状态" prop="status" width="100" />
          <el-table-column label="采购金额" prop="purchasePrice" width="120" />
          <el-table-column label="净值" prop="netAmount" width="120" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header><span>分类汇总</span></template>
        <el-table :data="categorySummaryList" stripe v-loading="loading">
          <el-table-column label="分类编号" prop="categoryId" width="100" />
          <el-table-column label="分类名称" prop="categoryName" min-width="180" />
          <el-table-column label="资产数量" prop="assetCount" width="100" />
          <el-table-column label="原值总额" prop="originalAmountTotal" min-width="120" />
          <el-table-column label="净值总额" prop="netAmountTotal" min-width="120" />
        </el-table>
      </el-card>
    </div>

    <el-card shadow="never" class="page-card">
      <template #header><span>折旧执行记录</span></template>
      <el-table :data="depreciationList" stripe v-loading="loading">
        <el-table-column label="月份" prop="runMonth" width="110" />
        <el-table-column label="处理数" prop="processedCount" width="90" />
        <el-table-column label="跳过数" prop="skippedCount" width="90" />
        <el-table-column label="当月折旧额" prop="totalMonthlyDepreciation" min-width="120" />
        <el-table-column label="状态" prop="status" width="100" />
        <el-table-column label="开始时间" prop="startedAt" min-width="160" />
        <el-table-column label="完成时间" prop="completedAt" min-width="160" />
      </el-table>
    </el-card>
  </div>
</template>
