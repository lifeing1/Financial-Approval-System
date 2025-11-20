package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * 出差统计
     */
    @Operation(summary = "出差统计")
    @GetMapping("/business-trip")
    public Result<?> getTripStatistics(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getTripStatistics(startDate, endDate));
    }
    
    /**
     * 备用金统计
     */
    @Operation(summary = "备用金统计")
    @GetMapping("/petty-cash")
    public Result<?> getCashStatistics(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getCashStatistics(startDate, endDate));
    }
}
