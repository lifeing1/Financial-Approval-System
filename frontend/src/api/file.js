import request from './request'

/**
 * 文件上传相关接口
 */

// 上传文件到OSS
export function uploadToOss(file, folder = 'expense/') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  return request({
    url: '/file/upload-oss',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 从OSS删除文件
export function deleteFromOss(fileUrl) {
  return request({
    url: '/file/delete-oss',
    method: 'delete',
    params: { fileUrl }
  })
}
