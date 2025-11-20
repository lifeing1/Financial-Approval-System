package com.approval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程节点配置表
 */
@Data
@TableName("process_node")
public class ProcessNode implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流程定义KEY
     */
    private String processKey;
    
    /**
     * 节点KEY（与BPMN中的userTask id对应）
     */
    private String nodeKey;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点类型（user_task-用户任务，service_task-服务任务，gateway-网关）
     */
    private String nodeType;
    
    /**
     * 审批人类型（user-指定用户，role-指定角色，dept_leader-部门负责人，initiator-发起人）
     */
    private String assigneeType;
    
    /**
     * 审批人配置（JSON格式，存储用户ID、角色ID等）
     */
    private String assigneeConfig;
    
    /**
     * 是否允许驳回（0-不允许，1-允许）
     */
    private Integer allowReject;
    
    /**
     * 是否需要附件（0-不需要，1-需要）
     */
    private Integer requireAttachment;
    
    /**
     * 审批超时小时数（0表示不限制）
     */
    private Integer timeoutHours;
    
    /**
     * 超时提醒配置（JSON格式）
     */
    private String timeoutReminder;
    
    /**
     * 节点顺序
     */
    private Integer nodeOrder;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
