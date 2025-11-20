package com.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 出差申请 DTO
 */
@Data
public class BusinessTripDTO {
    
    private Long id;
    
    @NotBlank(message = "出差事由不能为空")
    private String reason;
    
    @NotBlank(message = "出发地不能为空")
    private String startPlace;
    
    @NotBlank(message = "目的地不能为空")
    private String destination;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    private String transportModes;
    
    private String remark;
    
    private List<TripExpenseDTO> expenses;
}
