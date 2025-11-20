package com.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程定义DTO
 */
@Data
public class ProcessDefinitionDTO {
    
    /**
     * 流程定义KEY
     */
    private String processKey;
    
    /**
     * 流程名称
     */
    private String processName;
    
    /**
     * 流程分类
     */
    private String category;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 部署ID
     */
    private String deploymentId;
    
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 状态（0-暂停，1-激活）
     */
    private Integer status;
    
    /**
     * 部署时间
     */
    private LocalDateTime deployTime;
    
    /**
     * 流程节点列表
     */
    private List<ProcessNodeDTO> nodes;
}
