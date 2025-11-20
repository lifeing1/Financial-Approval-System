package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 附件实体
 */
@Data
@TableName("biz_attachment")
public class Attachment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long businessId;
    
    private String businessType;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileType;
    
    private Long uploadUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
