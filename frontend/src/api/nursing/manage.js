import request from '@/utils/request'

// 查询护理计划列表
export function listPlans(query) {
  return request({
    url: '/nursing/plan/list',
    method: 'get',
    params: query
  })
}

// 查询护理计划详情
export function getPlan(id) {
  return request({
    url: '/nursing/plan/' + id,
    method: 'get'
  })
}

// 新增或修改护理计划
export function savePlan(data) {
  return request({
    url: '/nursing/plan',
    method: data.id ? 'put' : 'post',
    data: data
  })
}

// 删除护理计划
export function delPlans(ids) {
  return request({
    url: '/nursing/plan/' + ids,
    method: 'delete'
  })
}

// 查询可关联护理项目
export function listProjects() {
  return request({
    url: '/nursing/plan/projects',
    method: 'get'
  })
}

// 查询护理等级列表
export function listLevels(query) {
  return request({
    url: '/nursing/level/list',
    method: 'get',
    params: query
  })
}

// 查询护理等级详情
export function getLevel(id) {
  return request({
    url: '/nursing/level/' + id,
    method: 'get'
  })
}

// 新增或修改护理等级
export function saveLevel(data) {
  return request({
    url: '/nursing/level',
    method: data.id ? 'put' : 'post',
    data: data
  })
}

// 删除护理等级
export function delLevels(ids) {
  return request({
    url: '/nursing/level/' + ids,
    method: 'delete'
  })
}

// 查询启用状态的护理计划
export function enabledPlans() {
  return request({
    url: '/nursing/level/enabledPlans',
    method: 'get'
  })
}
