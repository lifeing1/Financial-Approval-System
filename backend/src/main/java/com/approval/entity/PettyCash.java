package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 备用金申请实体
 */
@Data
@TableName("biz_petty_cash")
public class PettyCash {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String applyNo;
    
    private Long userId;
    
    private Long deptId;
    
    private LocalDate applyDate;
    
    private String reason;
    
    private BigDecimal amount;
    
    private String usePeriod;
    
    private String repaymentPlan;
    
    private String remark;
    
    /**
     * 状态: 0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回
     */
    private Integer status;
    
    private String processInstanceId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
