package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批意见实体
 */
@Data
@TableName("biz_approval_opinion")
public class ApprovalOpinion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long businessId;
    
    private String businessType;
    
    private String taskId;
    
    private Long approverId;
    
    private String approverName;
    
    private String opinion;
    
    private String action;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
