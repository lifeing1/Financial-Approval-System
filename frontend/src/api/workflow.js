import request from './request'

/**
 * 获取流程定义列表
 */
export function getProcessDefinitionList(params) {
  return request({
    url: '/workflow/definition/list',
    method: 'get',
    params
  })
}

/**
 * 获取流程定义详情
 */
export function getProcessDefinitionDetail(processKey) {
  return request({
    url: `/workflow/definition/${processKey}`,
    method: 'get'
  })
}

/**
 * 获取流程的所有节点信息
 */
export function getProcessNodes(processKey) {
  return request({
    url: `/workflow/nodes/${processKey}`,
    method: 'get'
  })
}

/**
 * 部署流程（文件上传）
 */
export function deployProcessByFile(data) {
  return request({
    url: '/workflow/deploy/file',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取预置BPMN文件列表
 */
export function getPresetBpmnFiles() {
  return request({
    url: '/workflow/preset-files',
    method: 'get'
  })
}

/**
 * 部署预置BPMN文件
 */
export function deployPresetBpmn(data) {
  return request({
    url: '/workflow/deploy/preset',
    method: 'post',
    params: data
  })
}

/**
 * 部署流程（设计器）
 */
export function deployProcessByDesign(data) {
  return request({
    url: '/workflow/deploy/design',
    method: 'post',
    data
  })
}

/**
 * 删除流程部署
 */
export function deleteDeployment(deploymentId, cascade = false) {
  return request({
    url: `/workflow/deploy/${deploymentId}`,
    method: 'delete',
    params: { cascade }
  })
}

/**
 * 激活流程
 */
export function activateProcess(processKey) {
  return request({
    url: `/workflow/activate/${processKey}`,
    method: 'post'
  })
}

/**
 * 暂停流程
 */
export function suspendProcess(processKey) {
  return request({
    url: `/workflow/suspend/${processKey}`,
    method: 'post'
  })
}

/**
 * 获取流程XML
 */
export function getProcessXml(processDefinitionId) {
  return request({
    url: `/workflow/xml/${processDefinitionId}`,
    method: 'get'
  })
}

/**
 * 导出流程BPMN文件
 */
export function exportProcessBpmn(processDefinitionId) {
  return request({
    url: `/workflow/export/${processDefinitionId}`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 获取流程历史版本
 */
export function getProcessVersions(processKey) {
  return request({
    url: `/workflow/versions/${processKey}`,
    method: 'get'
  })
}