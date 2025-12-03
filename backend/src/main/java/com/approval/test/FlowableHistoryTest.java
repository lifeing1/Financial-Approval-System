package com.approval.test;

import org.flowable.engine.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowableHistoryTest {
    
    @Autowired
    private HistoryService historyService;
    
    public void testHistoryService() {
        try {
            // 测试历史服务是否能正常创建查询对象
            Object query = historyService.createHistoricTaskInstanceQuery();
            System.out.println("History service is working. Query object: " + query.getClass().getName());
        } catch (Exception e) {
            System.err.println("Error testing history service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}