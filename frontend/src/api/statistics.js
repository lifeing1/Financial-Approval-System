import request from './request'

/**
 * 统计分析相关接口
 */

// 出差统计
export function getTripStatistics(params) {
  return request({
    url: '/statistics/business-trip',
    method: 'get',
    params
  })
}

// 备用金统计
export function getCashStatistics(params) {
  return request({
    url: '/statistics/petty-cash',
    method: 'get',
    params
  })
}
