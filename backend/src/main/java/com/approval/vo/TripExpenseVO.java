package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 出差费用明细 VO
 */
@Data
public class TripExpenseVO {
    
    private Long id;
    
    private Long tripId;
    
    private String expenseType;
    
    private BigDecimal amount;
    
    private String remark;
    
    /**
     * 附件列表
     */
    private List<AttachmentVO> attachments;
}
