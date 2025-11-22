package com.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 出差费用明细 DTO
 */
@Data
public class TripExpenseDTO {
    
    @NotBlank(message = "费用类型不能为空")
    private String expenseType;
    
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;
    
    private String remark;
    
    /**
     * 附件URL（多个文件用逗号分隔）
     */
    private String attachments;
}
