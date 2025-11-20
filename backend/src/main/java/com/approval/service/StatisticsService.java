package com.approval.service;

import com.approval.vo.CashStatisticsVO;
import com.approval.vo.TripStatisticsVO;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 出差统计
     */
    TripStatisticsVO getTripStatistics(String startDate, String endDate);
    
    /**
     * 备用金统计
     */
    CashStatisticsVO getCashStatistics(String startDate, String endDate);
}
