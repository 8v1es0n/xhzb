import request from '@/utils/request'

// 查询排班列表
export function listDutyRoster(query) {
  return request({
    url: '/nursing/duty/roster/list',
    method: 'get',
    params: query
  })
}

// 查询排班详细
export function getDutyRoster(rosterId) {
  return request({
    url: '/nursing/duty/roster/' + rosterId,
    method: 'get'
  })
}

// 新增排班
export function addDutyRoster(data) {
  return request({
    url: '/nursing/duty/roster',
    method: 'post',
    data: data
  })
}

// 修改排班
export function updateDutyRoster(data) {
  return request({
    url: '/nursing/duty/roster',
    method: 'put',
    data: data
  })
}

// 删除排班
export function delDutyRoster(rosterId) {
  return request({
    url: '/nursing/duty/roster/' + rosterId,
    method: 'delete'
  })
}

// 获取当前在岗人员
export function getCurrentDutyUsers() {
  return request({
    url: '/nursing/duty/roster/current',
    method: 'get'
  })
}
