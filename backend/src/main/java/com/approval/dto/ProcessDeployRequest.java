package com.approval.dto;

import lombok.Data;

import java.util.List;

/**
 * 流程部署请求
 */
@Data
public class ProcessDeployRequest {
    
    /**
     * 流程定义KEY（新建时可为空，更新时必填）
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
     * BPMN XML内容
     */
    private String bpmnXml;
    
    /**
     * 流程节点配置
     */
    private List<ProcessNodeDTO> nodes;
}
