package com.approval.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 财务任务监听器
 */
@Slf4j
@Component
public class FinanceTaskListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("财务审批任务创建，任务ID：{}，任务名称：{}", 
                delegateTask.getId(), delegateTask.getName());
    }
}
