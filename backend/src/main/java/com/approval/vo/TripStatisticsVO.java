package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 出差统计 VO
 */
@Data
public class TripStatisticsVO {
    
    /**
     * 按部门统计
     */
    private List<DeptStatItem> deptStats;
    
    /**
     * 按月份统计
     */
    private List<MonthStatItem> monthStats;
    
    /**
     * 按申请人统计
     */
    private List<UserStatItem> userStats;
    
    @Data
    public static class DeptStatItem {
        private String deptName;
        private Long count;
        private BigDecimal totalAmount;
    }
    
    @Data
    public static class MonthStatItem {
        private String month;
        private Long count;
        private BigDecimal totalAmount;
    }
    
    @Data
    public static class UserStatItem {
        private String userName;
        private Long count;
        private BigDecimal totalAmount;
    }
}
