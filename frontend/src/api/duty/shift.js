import request from '@/utils/request'

// 查询班次配置列表
export function listShiftConfig(query) {
  return request({
    url: '/nursing/shiftConfig/list',
    method: 'get',
    params: query
  })
}

// 获取所有班次（下拉框用）
export function allShifts() {
  return request({
    url: '/nursing/shiftConfig/all',
    method: 'get'
  })
}

// 查询班次配置详细
export function getShiftConfig(shiftId) {
  return request({
    url: '/nursing/shiftConfig/' + shiftId,
    method: 'get'
  })
}

// 新增班次配置
export function addShiftConfig(data) {
  return request({
    url: '/nursing/shiftConfig',
    method: 'post',
    data: data
  })
}

// 修改班次配置
export function updateShiftConfig(data) {
  return request({
    url: '/nursing/shiftConfig',
    method: 'put',
    data: data
  })
}

// 删除班次配置
export function delShiftConfig(shiftId) {
  return request({
    url: '/nursing/shiftConfig/' + shiftId,
    method: 'delete'
  })
}
