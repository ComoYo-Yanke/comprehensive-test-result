<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card><div ref="schoolChart" style="height: 300px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="clazzChart" style="height: 300px"></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card><div ref="boardChart" style="height: 300px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="activityChart" style="height: 300px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { employeeApi } from '../api'

const schoolChart = ref(null)
const clazzChart = ref(null)
const boardChart = ref(null)
const activityChart = ref(null)

function bar(el, names, values, color) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: values, itemStyle: { color }, barMaxWidth: 30 }]
  })
  return chart
}

function pie(el, data) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: '60%', data }]
  })
  return chart
}

onMounted(async () => {
  const stats = await employeeApi.statistics()

  const schoolNames = stats.schoolAverages.map(i => i.name)
  const schoolValues = stats.schoolAverages.map(i => i.avg ?? 0)
  bar(schoolChart.value, schoolNames, schoolValues, '#409eff')

  const clazzNames = stats.clazzAverages.map(i => i.name)
  const clazzValues = stats.clazzAverages.map(i => i.avg ?? 0)
  bar(clazzChart.value, clazzNames, clazzValues, '#67c23a')

  const board = stats.boardComparison
  const boardChart = echarts.init(boardChart.value)
  boardChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(board) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: Object.values(board), itemStyle: { color: '#e6a23c' }, barMaxWidth: 40 }]
  })

  const as = stats.activityStats
  pie(activityChart.value, [
    { name: '未审核', value: as['未审核'] },
    { name: '审核通过', value: as['审核通过'] },
    { name: '审核不通过', value: as['审核不通过'] },
    { name: '已结束', value: as['已结束'] }
  ])
})
</script>
