package com.approval.service.impl;

import com.approval.mapper.BusinessTripMapper;
import com.approval.mapper.PettyCashMapper;
import com.approval.service.StatisticsService;
import com.approval.vo.CashStatisticsVO;
import com.approval.vo.TripStatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private final BusinessTripMapper businessTripMapper;
    private final PettyCashMapper pettyCashMapper;
    
    @Override
    public TripStatisticsVO getTripStatistics(String startDate, String endDate) {
        // TODO: 实现具体统计逻辑
        TripStatisticsVO vo = new TripStatisticsVO();
        vo.setDeptStats(new ArrayList<>());
        vo.setMonthStats(new ArrayList<>());
        vo.setUserStats(new ArrayList<>());
        return vo;
    }
    
    @Override
    public CashStatisticsVO getCashStatistics(String startDate, String endDate) {
        // TODO: 实现具体统计逻辑
        CashStatisticsVO vo = new CashStatisticsVO();
        vo.setDeptStats(new ArrayList<>());
        vo.setMonthStats(new ArrayList<>());
        return vo;
    }
}
