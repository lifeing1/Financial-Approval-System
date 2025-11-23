package com.approval.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 附件 VO
 */
@Data
public class AttachmentVO {
    
    private Long id;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileType;
    
    private LocalDateTime createTime;
}
