package com.approval.dto;

import lombok.Data;

/**
 * 流程节点DTO
 */
@Data
public class ProcessNodeDTO {
    
    /**
     * 节点ID
     */
    private Long id;
    
    /**
     * 节点KEY
     */
    private String nodeKey;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点类型
     */
    private String nodeType;
    
    /**
     * 审批人类型
     */
    private String assigneeType;
    
    /**
     * 审批人配置
     */
    private String assigneeConfig;
    
    /**
     * 是否允许驳回
     */
    private Integer allowReject;
    
    /**
     * 是否需要附件
     */
    private Integer requireAttachment;
    
    /**
     * 超时小时数
     */
    private Integer timeoutHours;
    
    /**
     * 超时提醒配置
     */
    private String timeoutReminder;
    
    /**
     * 节点顺序
     */
    private Integer nodeOrder;
}
