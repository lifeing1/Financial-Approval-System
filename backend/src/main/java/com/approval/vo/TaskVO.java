package com.approval.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务 VO
 */
@Data
public class TaskVO {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 流程实例ID
     */
    private String processInstanceId;
    
    /**
     * 流程定义KEY
     */
    private String processKey;
    
    /**
     * 流程定义名称
     */
    private String processName;
    
    /**
     * 业务KEY
     */
    private String businessKey;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 申请编号
     */
    private String applyNo;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 申请事由
     */
    private String reason;
    
    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 优先级
     */
    private Integer priority;
}
