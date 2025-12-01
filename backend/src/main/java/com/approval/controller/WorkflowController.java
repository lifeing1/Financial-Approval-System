package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.dto.ProcessDeployRequest;
import com.approval.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 工作流管理控制器
 */
@Tag(name = "工作流管理")
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * 部署流程（文件上传）
     */
    @Operation(summary = "部署流程（文件上传）")
    @PostMapping("/deploy/file")
    public Result<?> deployProcessByFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("processName") String processName) {
        String deploymentId = workflowService.deployProcess(file, processName);
        return Result.success(deploymentId);
    }
    
    /**
     * 获取预置BPMN文件列表
     */
    @Operation(summary = "获取预置BPMN文件列表")
    @GetMapping("/preset-files")
    public Result<?> getPresetBpmnFiles() {
        return Result.success(workflowService.getPresetBpmnFiles());
    }
    
    /**
     * 部署预置BPMN文件
     */
    @Operation(summary = "部署预置BPMN文件")
    @PostMapping("/deploy/preset")
    public Result<?> deployPresetBpmn(@RequestParam("fileName") String fileName,
                                      @RequestParam("processName") String processName) {
        String deploymentId = workflowService.deployPresetBpmn(fileName, processName);
        return Result.success(deploymentId);
    }
    
    /**
     * 部署流程（可视化设计）
     */
    @Operation(summary = "部署流程（可视化设计）")
    @PostMapping("/deploy/design")
    public Result<?> deployProcessByDesign(@RequestBody ProcessDeployRequest request) {
        String deploymentId = workflowService.deployProcessByDesign(request);
        return Result.success(deploymentId);
    }
    
    /**
     * 删除流程部署
     */
    @Operation(summary = "删除流程部署")
    @DeleteMapping("/deploy/{deploymentId}")
    public Result<?> deleteDeployment(@PathVariable String deploymentId,
                                      @RequestParam(defaultValue = "false") boolean cascade) {
        workflowService.deleteDeployment(deploymentId, cascade);
        return Result.success("删除成功");
    }
    
    /**
     * 获取流程定义列表
     */
    @Operation(summary = "获取流程定义列表")
    @GetMapping("/definition/list")
    public Result<?> getProcessDefinitionList(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String category) {
        return Result.success(workflowService.getProcessDefinitionList(current, size, category));
    }
    
    /**
     * 获取流程定义详情
     */
    @Operation(summary = "获取流程定义详情")
    @GetMapping("/definition/{processKey}")
    public Result<?> getProcessDefinitionDetail(@PathVariable String processKey) {
        return Result.success(workflowService.getProcessDefinitionDetail(processKey));
    }
    
    /**
     * 激活流程
     */
    @Operation(summary = "激活流程")
    @PostMapping("/activate/{processKey}")
    public Result<?> activateProcess(@PathVariable String processKey) {
        workflowService.activateProcess(processKey);
        return Result.success("流程已激活");
    }
    
    /**
     * 暂停流程
     */
    @Operation(summary = "暂停流程")
    @PostMapping("/suspend/{processKey}")
    public Result<?> suspendProcess(@PathVariable String processKey) {
        workflowService.suspendProcess(processKey);
        return Result.success("流程已暂停");
    }
    
    /**
     * 获取流程XML
     */
    @Operation(summary = "获取流程XML")
    @GetMapping("/xml/{processDefinitionId}")
    public Result<?> getProcessXml(@PathVariable String processDefinitionId) {
        return Result.success(workflowService.getProcessXml(processDefinitionId));
    }
    
    /**
     * 导出流程BPMN文件
     */
    @Operation(summary = "导出流程BPMN文件")
    @GetMapping("/export/{processDefinitionId}")
    public ResponseEntity<byte[]> exportProcessBpmn(@PathVariable String processDefinitionId) {
        byte[] bpmnData = workflowService.exportProcessBpmn(processDefinitionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment", processDefinitionId + ".bpmn20.xml");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(bpmnData);
    }
    
    /**
     * 获取流程历史版本
     */
    @Operation(summary = "获取流程历史版本")
    @GetMapping("/versions/{processKey}")
    public Result<?> getProcessVersions(@PathVariable String processKey) {
        return Result.success(workflowService.getProcessVersions(processKey));
    }
    
    /**
     * 启动流程实例
     */
    @Operation(summary = "启动流程实例")
    @PostMapping("/start")
    public Result<?> startProcess(@RequestParam String processKey,
                                  @RequestParam String businessKey,
                                  @RequestParam Long userId,
                                  @RequestParam(required = false) Long deptId) {
        String processInstanceId = workflowService.startProcess(processKey, businessKey, userId, deptId);
        return Result.success(processInstanceId);
    }
    
    /**
     * 完成任务
     */
    @Operation(summary = "完成任务")
    @PostMapping("/task/complete")
    public Result<?> completeTask(@RequestParam String taskId,
                                  @RequestParam(required = false) String comment,
                                  @RequestParam boolean approved) {
        workflowService.completeTask(taskId, comment, approved);
        return Result.success("操作成功");
    }
    
    /**
     * 获取用户待办任务
     */
    @Operation(summary = "获取用户待办任务")
    @GetMapping("/task/user/{userId}")
    public Result<?> getUserTasks(@PathVariable Long userId,
                                  @RequestParam(defaultValue = "1") Integer current,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(workflowService.getUserTasks(userId, current, size));
    }
}
