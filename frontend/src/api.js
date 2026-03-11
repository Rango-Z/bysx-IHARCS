import axios from 'axios'

export async function fetchRealtimeDevices() {
  const { data } = await axios.get('/api/datav/realtime/devices')
  return data?.data ?? []
}

export async function fetchUsage(days = 7) {
  const { data } = await axios.get('/api/datav/stat/usage', { params: { days } })
  return data?.data
}

export async function fetchEnergy(days = 7) {
  const { data } = await axios.get('/api/datav/stat/energy', { params: { days } })
  return data?.data
}

export async function fetchWasherProgress() {
  const { data } = await axios.get('/api/datav/realtime/washer-progress')
  return data?.data
}

export async function sendControl(payload) {
  const { data } = await axios.post('/api/control/send', payload)
  return data?.data
}

export async function fetchControlResult(commandId) {
  const { data } = await axios.get(`/api/control/result/${commandId}`)
  return data?.data
}

