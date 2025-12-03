package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 备用金申请 VO
 */
@Data
public class PettyCashVO {
    
    private Long id;
    
    private String applyNo;
    
    private Long userId;
    
    private String userName;
    
    private Long deptId;
    
    private String deptName;
    
    private LocalDate applyDate;
    
    private String reason;
    
    private BigDecimal amount;
    
    private String usePeriod;
    
    private String repaymentPlan;
    
    private String remark;
    
    private Integer status;
    
    private String processInstanceId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}