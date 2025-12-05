package com.approval.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务 VO
 */
@Data
public class TaskVO {
    
    /**
     * 业务ID（出差申请ID或备用金申请ID）
     */
    private Long id;
    
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
     * 申请人姓名（别名，与applicantName相同）
     */
    private String userName;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 申请事由
     */
    private String reason;
    
    /**
     * 目的地（出差申请）
     */
    private String destination;
    
    /**
     * 开始日期（出差申请）
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（出差申请）
     */
    private LocalDate endDate;
    
    /**
     * 预计费用/申请金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 审批完成时间
     */
    private LocalDateTime approveTime;

    /**
     * 状态: 0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回
     */
    private Integer status;
    
    /**
     * 优先级
     */
    private Integer priority;
}