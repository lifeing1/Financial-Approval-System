package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出差申请 VO
 */
@Data
public class BusinessTripVO {
    
    private Long id;
    
    private String applyNo;
    
    private Long userId;
    
    private String userName;
    
    private Long deptId;
    
    private String deptName;
    
    private LocalDate applyDate;
    
    private String reason;
    
    private String startPlace;
    
    private String destination;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String transportModes;
    
    private BigDecimal totalAmount;
    
    private String remark;
    
    private Integer status;
    
    private String processInstanceId;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    /**
     * 费用明细列表
     */
    private List<TripExpenseVO> expenses;
}
