package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出差费用明细实体
 */
@Data
@TableName("biz_trip_expense")
public class TripExpense {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long tripId;
    
    private String expenseType;
    
    private BigDecimal amount;
    
    private String remark;
    
    /**
     * 附件URL（多个文件用逗号分隔）
     */
    private String attachments;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
