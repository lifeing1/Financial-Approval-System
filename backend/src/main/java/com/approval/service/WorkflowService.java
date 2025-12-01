package com.approval.service;

import com.approval.dto.ProcessDefinitionDTO;
import com.approval.dto.ProcessDeployRequest;
import com.approval.vo.TaskVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 工作流服务接口
 */
public interface WorkflowService {
    
    /**
     * 部署流程（文件上传方式）
     */
    String deployProcess(MultipartFile file, String processName);
    
    /**
     * 获取预置的BPMN文件列表
     */
    List<String> getPresetBpmnFiles();
    
    /**
     * 部署预置的BPMN文件
     */
    String deployPresetBpmn(String fileName, String processName);
    
    /**
     * 部署流程（设计器方式）
     */
    String deployProcessByDesign(ProcessDeployRequest request);
    
    /**
     * 删除流程部署
     */
    void deleteDeployment(String deploymentId, boolean cascade);
    
    /**
     * 获取流程定义列表
     */
    Page<ProcessDefinitionDTO> getProcessDefinitionList(Integer current, Integer size, String category);
    
    /**
     * 获取流程定义详情
     */
    ProcessDefinitionDTO getProcessDefinitionDetail(String processKey);
    
    /**
     * 激活流程
     */
    void activateProcess(String processKey);
    
    /**
     * 暂停流程
     */
    void suspendProcess(String processKey);
    
    /**
     * 获取流程XML
     */
    String getProcessXml(String processDefinitionId);
    
    /**
     * 导出流程BPMN文件
     */
    byte[] exportProcessBpmn(String processDefinitionId);
    
    /**
     * 获取流程历史版本列表
     */
    List<ProcessDefinitionDTO> getProcessVersions(String processKey);
    
    /**
     * 启动流程实例
     */
    String startProcess(String processKey, String businessKey, Long userId, Long deptId);
    
    /**
     * 完成任务
     */
    void completeTask(String taskId, String comment, boolean approved);
    
    /**
     * 获取用户待办任务
     */
    Page<TaskVO> getUserTasks(Long userId, Integer current, Integer size);
}
