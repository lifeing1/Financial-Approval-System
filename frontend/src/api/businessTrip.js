import request from './request'

/**
 * 保存草稿
 */
export function saveDraft(data) {
  return request({
    url: '/business-trip/draft',
    method: 'post',
    data
  })
}

/**
 * 提交申请
 */
export function submitApply(id) {
  return request({
    url: `/business-trip/submit/${id}`,
    method: 'post'
  })
}

/**
 * 我的申请列表
 */
export function getMyList(params) {
  return request({
    url: '/business-trip/my',
    method: 'get',
    params
  })
}

/**
 * 待办列表
 */
export function getTodoList(params) {
  return request({
    url: '/business-trip/todo',
    method: 'get',
    params
  })
}

/**
 * 已办列表
 */
export function getDoneList(params) {
  return request({
    url: '/business-trip/done',
    method: 'get',
    params
  })
}

/**
 * 查询详情
 */
export function getDetail(id) {
  return request({
    url: `/business-trip/${id}`,
    method: 'get'
  })
}

/**
 * 审批通过
 */
export function approve(id, opinion) {
  return request({
    url: `/business-trip/approve/${id}`,
    method: 'post',
    params: { opinion }
  })
}

/**
 * 审批驳回
 */
export function reject(id, opinion) {
  return request({
    url: `/business-trip/reject/${id}`,
    method: 'post',
    params: { opinion }
  })
}

/**
 * 获取审批历史记录
 */
export function getApprovalHistory(id) {
  return request({
    url: `/business-trip/history/${id}`,
    method: 'get'
  })
}