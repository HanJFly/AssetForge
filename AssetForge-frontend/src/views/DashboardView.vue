<script setup>
import StatCard from '@/components/StatCard.vue'

const quickModules = [
  { name: '部门管理', desc: '维护组织架构与负责人信息' },
  { name: '资产管理', desc: '资产登记、台账、附件与条码' },
  { name: '审批中心', desc: '待办审批与历史审批记录' },
  { name: '盘点管理', desc: '盘点任务、盘亏处理与报告' }
]
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-desc">查看系统概览与核心业务入口。</p>
      </div>
      <el-tag type="success" size="large">运行中</el-tag>
    </div>

    <div class="stats-grid">
      <StatCard title="已覆盖模块" :value="12" desc="组织、资产、审批、业务、盘点、报表" accent="#2563eb" />
      <StatCard title="接口分组" :value="13" desc="围绕核心业务模块组织" accent="#0f766e" />
      <StatCard title="页面类型" :value="'CRUD + 流程'" desc="列表、表单与流程页面" accent="#ea580c" />
      <StatCard title="系统状态" :value="'可用'" desc="支持日常业务操作" accent="#7c3aed" />
    </div>

    <div class="split-grid">
      <el-card shadow="never" class="page-card">
        <template #header>
          <div class="card-title-row">
            <span>模块概览</span>
            <el-tag>已启用</el-tag>
          </div>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="item in quickModules"
            :key="item.name"
            type="primary"
            hollow
          >
            <strong>{{ item.name }}</strong>
            <p class="muted-text">{{ item.desc }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header>
          <div class="card-title-row">
            <span>运行提示</span>
            <el-tag type="warning">请及时处理待办</el-tag>
          </div>
        </template>

        <el-space direction="vertical" fill>
          <el-alert title="建议优先检查基础资料、资产和审批主流程。" type="info" show-icon :closable="false" />
          <el-alert title="盘点、报表与折旧建议在主数据稳定后集中回归。" type="success" show-icon :closable="false" />
        </el-space>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
