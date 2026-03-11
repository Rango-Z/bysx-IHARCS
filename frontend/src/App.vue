<template>
  <div class="container">
    <div class="topbar">
      <div class="title">
        <h1>智能家电远程控制系统 · 大屏</h1>
        <small>Spring Cloud · MQTT · MongoDB · MySQL · DataV/ECharts</small>
      </div>
      <div class="statusline">当前时间：{{ nowText }} · 指令状态：{{ lastCommandText }}</div>
    </div>

    <div class="grid">
      <div class="panel">
        <h2>家电实时状态</h2>
        <div class="cards">
          <div v-for="d in devices" :key="d.deviceId" class="card">
            <div class="card-head">
              <div>
                <div style="font-weight: 700">{{ d.name || d.deviceId }}</div>
                <div style="color: var(--muted); font-size: 12px">{{ d.deviceType }} · {{ d.deviceId }}</div>
              </div>
              <span class="badge">{{ d.timestamp ? '实时' : '暂无数据' }}</span>
            </div>
            <div class="kv">
              <div class="k">上报时间</div><div>{{ d.timestamp ? formatTime(d.timestamp) : '-' }}</div>
              <div class="k">电源</div><div>{{ show(d.status?.power) }}</div>
              <div class="k">模式</div><div>{{ show(d.status?.mode) }}</div>
              <div class="k">设定温度</div><div>{{ show(d.status?.tempSetting) }}</div>
              <div class="k">当前温度</div><div>{{ show(d.status?.currentTemp) }}</div>
              <div class="k">亮度</div><div>{{ show(d.status?.brightness) }}</div>
              <div class="k">洗衣进度</div><div>{{ show(d.status?.progress) }}</div>
              <div class="k">当前功率(W)</div><div>{{ show(d.energy?.powerW) }}</div>
              <div class="k">累计电量(kWh)</div><div>{{ show(d.energy?.kwhTotal) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <h2>远程控制</h2>
        <div class="controls">
          <div class="control-box">
            <h3>空调（ac001）</h3>
            <div class="btn-row">
              <button @click="ctrl('ac001','POWER', { power:'ON' })">开机</button>
              <button class="secondary" @click="ctrl('ac001','POWER', { power:'OFF' })">关机</button>
              <button class="secondary" @click="ctrl('ac001','SET_TEMP', { temp: 26 })">设为 26℃</button>
              <button class="secondary" @click="ctrl('ac001','SET_TEMP', { temp: 24 })">设为 24℃</button>
              <button class="secondary" @click="ctrl('ac001','SET_MODE', { mode: 'COOL' })">制冷</button>
              <button class="secondary" @click="ctrl('ac001','SET_MODE', { mode: 'HEAT' })">制热</button>
            </div>
          </div>

          <div class="control-box">
            <h3>冰箱（fr001）</h3>
            <div class="btn-row">
              <button @click="ctrl('fr001','SET_FRIDGE_TEMP', { cold: 4, freeze: -18 })">冷藏4/冷冻-18</button>
              <button class="secondary" @click="ctrl('fr001','SET_FRIDGE_TEMP', { cold: 2, freeze: -20 })">冷藏2/冷冻-20</button>
            </div>
          </div>

          <div class="control-box">
            <h3>洗衣机（wa001）</h3>
            <div class="btn-row">
              <button @click="ctrl('wa001','START', { mode:'COTTON' })">启动（棉麻）</button>
              <button class="secondary" @click="ctrl('wa001','PAUSE', {})">暂停</button>
              <button class="secondary" @click="ctrl('wa001','RESUME', {})">继续</button>
              <button class="danger" @click="ctrl('wa001','STOP', {})">停止</button>
            </div>
          </div>

          <div class="control-box">
            <h3>灯光（li001）</h3>
            <div class="btn-row">
              <button @click="ctrl('li001','POWER', { power:'ON' })">开灯</button>
              <button class="secondary" @click="ctrl('li001','POWER', { power:'OFF' })">关灯</button>
              <button class="secondary" @click="ctrl('li001','BRIGHTNESS', { brightness: 80 })">亮度 80%</button>
              <button class="secondary" @click="ctrl('li001','BRIGHTNESS', { brightness: 30 })">亮度 30%</button>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <h2>数据分析</h2>
        <div class="charts">
          <div class="chart tall">
            <EChart :option="usageOption" />
          </div>
          <div class="chart">
            <EChart :option="energyOption" />
          </div>
          <div class="chart">
            <EChart :option="washerOption" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import EChart from './components/EChart.vue'
import { fetchEnergy, fetchRealtimeDevices, fetchUsage, fetchWasherProgress, fetchControlResult, sendControl } from './api'

const nowText = ref('')
const devices = ref([])
const usage = ref(null)
const energy = ref(null)
const washer = ref({ deviceId: 'wa001', progress: 0 })

const lastCommand = ref(null) // {commandId, status, resultMessage}
const lastCommandText = computed(() => {
  if (!lastCommand.value) return '无'
  const s = lastCommand.value.status
  const label = s === 0 ? '待下发' : s === 1 ? '已下发' : s === 2 ? '成功' : '失败'
  return `${lastCommand.value.commandId} · ${label}${lastCommand.value.resultMessage ? ' · ' + lastCommand.value.resultMessage : ''}`
})

function tickTime() {
  const d = new Date()
  nowText.value = d.toLocaleString()
}

function formatTime(ts) {
  const d = new Date(ts)
  return d.toLocaleTimeString()
}

function show(v) {
  if (v === null || v === undefined || v === '') return '-'
  return String(v)
}

async function loadAll() {
  devices.value = await fetchRealtimeDevices()
  usage.value = await fetchUsage(7)
  energy.value = await fetchEnergy(7)
  washer.value = await fetchWasherProgress()
}

async function ctrl(deviceId, commandType, payload) {
  const r = await sendControl({ deviceId, commandType, payload })
  lastCommand.value = r
  // 轮询结果 8 次（~8s），用于大屏提示
  for (let i = 0; i < 8; i++) {
    await new Promise(resolve => setTimeout(resolve, 1000))
    const rr = await fetchControlResult(r.commandId)
    lastCommand.value = rr
    if (rr.status === 2 || rr.status === 3) break
  }
}

const usageOption = computed(() => {
  const u = usage.value
  const days = u?.days ?? []
  const seriesMap = u?.deviceHoursByDay ?? {}
  const deviceIds = Object.keys(seriesMap)
  return {
    title: { text: '近7天使用时长(小时)', textStyle: { color: '#e8ecff', fontSize: 12 } },
    tooltip: { trigger: 'axis' },
    legend: { data: deviceIds, textStyle: { color: '#9aa6d6' } },
    grid: { left: 36, right: 16, top: 42, bottom: 26 },
    xAxis: { type: 'category', data: days, axisLabel: { color: '#9aa6d6' } },
    yAxis: { type: 'value', axisLabel: { color: '#9aa6d6' } },
    series: deviceIds.map(id => ({
      name: id,
      type: 'bar',
      stack: 'total',
      emphasis: { focus: 'series' },
      data: seriesMap[id] ?? []
    }))
  }
})

const energyOption = computed(() => {
  const items = energy.value?.items ?? []
  return {
    title: { text: '能耗占比(kWh)', textStyle: { color: '#e8ecff', fontSize: 12 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#9aa6d6' } },
    series: [{
      type: 'pie',
      radius: ['35%', '70%'],
      itemStyle: { borderColor: 'rgba(255,255,255,.10)', borderWidth: 1 },
      label: { color: '#e8ecff' },
      data: items.map(i => ({ name: i.name, value: i.kwh }))
    }]
  }
})

const washerOption = computed(() => {
  const p = Number(washer.value?.progress ?? 0)
  return {
    title: { text: '洗衣机进度', textStyle: { color: '#e8ecff', fontSize: 12 } },
    series: [{
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      min: 0,
      max: 100,
      progress: { show: true, width: 12 },
      axisLine: { lineStyle: { width: 12 } },
      axisTick: { show: false },
      splitLine: { length: 10, lineStyle: { color: '#9aa6d6' } },
      axisLabel: { color: '#9aa6d6' },
      pointer: { show: true, width: 4 },
      detail: { valueAnimation: true, formatter: '{value}%', color: '#2ee6a6' },
      data: [{ value: p }]
    }]
  }
})

let timer1 = null
let timer2 = null

onMounted(async () => {
  tickTime()
  timer1 = setInterval(tickTime, 1000)
  await loadAll()
  timer2 = setInterval(loadAll, 3000)
})

onBeforeUnmount(() => {
  if (timer1) clearInterval(timer1)
  if (timer2) clearInterval(timer2)
})
</script>

