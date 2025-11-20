package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 备用金统计 VO
 */
@Data
public class CashStatisticsVO {
    
    /**
     * 按部门统计
     */
    private List<DeptStatItem> deptStats;
    
    /**
     * 按月份统计
     */
    private List<MonthStatItem> monthStats;
    
    @Data
    public static class DeptStatItem {
        private String deptName;
        private Long applyCount;
        private BigDecimal applyAmount;
        private Long approvedCount;
        private BigDecimal approvedAmount;
    }
    
    @Data
    public static class MonthStatItem {
        private String month;
        private Long applyCount;
        private BigDecimal applyAmount;
        private Long approvedCount;
        private BigDecimal approvedAmount;
    }
}
