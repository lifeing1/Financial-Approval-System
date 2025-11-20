package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 出差申请实体
 */
@Data
@TableName("biz_business_trip")
public class BusinessTrip {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String applyNo;
    
    private Long userId;
    
    private Long deptId;
    
    private LocalDate applyDate;
    
    private String reason;
    
    private String startPlace;
    
    private String destination;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String transportModes;
    
    private BigDecimal totalAmount;
    
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
