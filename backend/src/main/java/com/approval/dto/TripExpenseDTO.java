package com.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

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
     * 附件列表（包含文件名和URL）
     */
    private List<AttachmentDTO> attachments;
}
