package com.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 备用金申请 DTO
 */
@Data
public class PettyCashDTO {
    
    private Long id;
    
    @NotBlank(message = "申请事由不能为空")
    private String reason;
    
    @NotNull(message = "申请金额不能为空")
    private BigDecimal amount;
    
    private String usePeriod;
    
    private String repaymentPlan;
    
    private String remark;
}
