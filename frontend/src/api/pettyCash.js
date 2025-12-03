import request from './request'

/**
 * 备用金申请相关接口
 */

// 保存草稿
export function saveDraft(data) {
  return request({
    url: '/petty-cash/draft',
    method: 'post',
    data
  })
}

// 提交申请
export function submitApply(id) {
  return request({
    url: `/petty-cash/submit/${id}`,
    method: 'post'
  })
}

// 我的申请列表
export function getMyList(params) {
  return request({
    url: '/petty-cash/my',
    method: 'get',
    params
  })
}

// 待办列表
export function getTodoList(params) {
  return request({
    url: '/petty-cash/todo',
    method: 'get',
    params
  })
}

// 已办列表
export function getDoneList(params) {
  return request({
    url: '/petty-cash/done',
    method: 'get',
    params
  })
}

// 查询详情
export function getDetail(id) {
  return request({
    url: `/petty-cash/${id}`,
    method: 'get'
  })
}

// 查询详情（包含用户和部门信息）
export function getDetailWithUserInfo(id) {
  return request({
    url: `/petty-cash/detail/${id}`,
    method: 'get'
  })
}

// 审批通过
export function approve(id, opinion) {
  return request({
    url: `/petty-cash/approve/${id}`,
    method: 'post',
    params: { opinion }
  })
}

// 审批驳回
export function reject(id, opinion) {
  return request({
    url: `/petty-cash/reject/${id}`,
    method: 'post',
    params: { opinion }
  })
}

// 获取审批历史记录
export function getApprovalHistory(id) {
  return request({
    url: `/petty-cash/history/${id}`,
    method: 'get'
  })
}
