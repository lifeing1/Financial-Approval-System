package com.approval.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String businessType, Long businessId);
    
    /**
     * 删除文件
     */
    void deleteFile(Long id);
    
    /**
     * 获取文件路径
     */
    String getFilePath(Long id);
}
