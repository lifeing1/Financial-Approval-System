package com.approval.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 部门经理任务监听器
 */
@Slf4j
@Component
public class DeptManagerTaskListener implements TaskListener {
    
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("部门经理审批任务创建，任务ID：{}，任务名称：{}", 
                delegateTask.getId(), delegateTask.getName());
    }
}
