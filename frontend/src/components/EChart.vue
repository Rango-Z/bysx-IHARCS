<template>
  <div ref="el" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true }
})

const el = ref(null)
let chart = null

function render() {
  if (!el.value) return
  if (!chart) chart = echarts.init(el.value)
  chart.setOption(props.option, true)
  chart.resize()
}

onMounted(() => {
  render()
  window.addEventListener('resize', render)
})

watch(() => props.option, () => render(), { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', render)
  if (chart) chart.dispose()
  chart = null
})
</script>

