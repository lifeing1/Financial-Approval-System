package com.approval.dto;

import lombok.Data;

/**
 * 附件 DTO
 */
@Data
public class AttachmentDTO {
    
    /**
     * 文件名（原始文件名）
     */
    private String fileName;
    
    /**
     * 文件URL（OSS存储路径）
     */
    private String url;
}
