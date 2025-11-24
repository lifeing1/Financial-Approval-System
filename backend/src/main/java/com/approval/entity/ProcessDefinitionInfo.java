package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程定义信息扩展表
 * 用于存储流程的业务信息和配置
 */
@Data
@TableName("process_definition_info")
public class ProcessDefinitionInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程定义KEY（唯一标识）
     */
    private String processKey;
    
    /**
     * 流程名称
     */
    private String processName;
    
    /**
     * 流程分类（business_trip-出差审批，petty_cash-备用金审批）
     */
    private String category;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 最新部署ID
     */
    private String deploymentId;
    
    /**
     * 最新流程定义ID
     */
    private String processDefinitionId;
    
    /**
     * 流程版本号
     */
    private Integer version;
    
    /**
     * 流程状态（0-暂停，1-激活）
     */
    private Integer status;
    
    /**
     * 流程配置（JSON格式，存储节点配置等）
     */
    private String processConfig;
    
    /**
     * 创建人
     */
    private Long createUser;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
