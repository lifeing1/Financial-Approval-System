package com.approval.service.impl;

import cn.hutool.core.util.StrUtil;
import com.approval.dto.ProcessDefinitionDTO;
import com.approval.dto.ProcessDeployRequest;
import com.approval.dto.ProcessNodeDTO;
import com.approval.entity.ApprovalOpinion;
import com.approval.entity.BusinessTrip;
import com.approval.entity.PettyCash;
import com.approval.entity.ProcessDefinitionInfo;
import com.approval.entity.ProcessNode;
import com.approval.entity.SysDept;
import com.approval.entity.SysUser;
import com.approval.mapper.ApprovalOpinionMapper;
import com.approval.mapper.BusinessTripMapper;
import com.approval.mapper.PettyCashMapper;
import com.approval.mapper.ProcessDefinitionInfoMapper;
import com.approval.mapper.ProcessNodeMapper;
import com.approval.mapper.SysDeptMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.service.WorkflowService;
import com.approval.vo.TaskVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {
    
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ProcessDefinitionInfoMapper definitionInfoMapper;
    private final ProcessNodeMapper nodeMapper;
    private final ApprovalOpinionMapper opinionMapper;
    private final BusinessTripMapper businessTripMapper;
    private final PettyCashMapper pettyCashMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    
    private static final String BPMN_RESOURCE_PATH = "classpath:processes/*.bpmn";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployProcess(MultipartFile file, String processName) {
        try {
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            
            // 1. 部署到 Flowable 引擎
            Deployment deployment = repositoryService.createDeployment()
                    .name(processName)
                    .addInputStream(fileName, inputStream)
                    .deploy();
            
            // 2. 获取流程定义信息
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义获取失败");
            }
            
            // 3. 保存或更新流程定义扩展信息
            ProcessDefinitionInfo existingInfo = definitionInfoMapper.selectOne(
                    new LambdaQueryWrapper<ProcessDefinitionInfo>()
                            .eq(ProcessDefinitionInfo::getProcessKey, processDefinition.getKey())
            );
            
            if (existingInfo != null) {
                // 更新现有记录
                existingInfo.setProcessName(processName);
                existingInfo.setDeploymentId(deployment.getId());
                existingInfo.setProcessDefinitionId(processDefinition.getId());
                existingInfo.setVersion(processDefinition.getVersion());
                existingInfo.setStatus(1); // 激活状态
                definitionInfoMapper.updateById(existingInfo);
            } else {
                // 新建记录
                ProcessDefinitionInfo definitionInfo = new ProcessDefinitionInfo();
                definitionInfo.setProcessKey(processDefinition.getKey());
                definitionInfo.setProcessName(processName);
                definitionInfo.setDeploymentId(deployment.getId());
                definitionInfo.setProcessDefinitionId(processDefinition.getId());
                definitionInfo.setVersion(processDefinition.getVersion());
                definitionInfo.setStatus(1);
                // 根据流程 key 猜测分类
                if (processDefinition.getKey().contains("businessTrip") || processDefinition.getKey().contains("trip")) {
                    definitionInfo.setCategory("business_trip");
                } else if (processDefinition.getKey().contains("pettyCash") || processDefinition.getKey().contains("cash")) {
                    definitionInfo.setCategory("petty_cash");
                }
                definitionInfoMapper.insert(definitionInfo);
            }
            
            log.info("流程部署成功，流程KEY：{}，版本：{}", processDefinition.getKey(), processDefinition.getVersion());
            return deployment.getId();
        } catch (Exception e) {
            log.error("流程部署失败", e);
            throw new RuntimeException("流程部署失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<String> getPresetBpmnFiles() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(BPMN_RESOURCE_PATH);
            
            return Arrays.stream(resources)
                    .map(resource -> {
                        try {
                            return resource.getFilename();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取预置BPMN文件列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployPresetBpmn(String fileName, String processName) {
        log.info("开始部署预置流程，文件名：{}，流程名称：{}", fileName, processName);
        try {
            // 1. 从 classpath 加载 BPMN 文件
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:processes/" + fileName);
            
            if (!resource.exists()) {
                throw new RuntimeException("BPMN文件不存在：" + fileName);
            }
            
            InputStream inputStream = resource.getInputStream();
            
            // 2. 部署到 Flowable 引擎
            log.info("开始部署到Flowable引擎...");
            Deployment deployment = repositoryService.createDeployment()
                    .name(processName)
                    .addInputStream(fileName, inputStream)
                    .deploy();
            log.info("Flowable部署成功，部署ID：{}", deployment.getId());
            
            // 3. 获取流程定义信息
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义获取失败");
            }
            log.info("获取流程定义成功，流程KEY：{}，流程ID：{}", 
                    processDefinition.getKey(), processDefinition.getId());
            
            // 4. 保存或更新流程定义扩展信息
            log.info("开始保存流程定义扩展信息到process_definition_info表...");
            
            // 先查询正常记录
            ProcessDefinitionInfo existingInfo = definitionInfoMapper.selectOne(
                    new LambdaQueryWrapper<ProcessDefinitionInfo>()
                            .eq(ProcessDefinitionInfo::getProcessKey, processDefinition.getKey())
            );
            
            // 如果没有正常记录，查询是否有已删除的记录
            if (existingInfo == null) {
                existingInfo = definitionInfoMapper.selectOne(
                        new LambdaQueryWrapper<ProcessDefinitionInfo>()
                                .eq(ProcessDefinitionInfo::getProcessKey, processDefinition.getKey())
                                .eq(ProcessDefinitionInfo::getDeleted, 1) // 查询已删除的记录
                                .last("LIMIT 1")
                );
                if (existingInfo != null) {
                    log.info("发现已删除的记录，ID：{}，准备恢复并更新", existingInfo.getId());
                    // 恢复记录（将 deleted 设为 0）
                    existingInfo.setDeleted(0);
                }
            }
            
            log.info("查询现有记录结果，existingInfo: {}", existingInfo != null ? existingInfo.getId() : "null");
            
            if (existingInfo != null) {
                // 更新现有记录
                existingInfo.setProcessName(processName);
                existingInfo.setDeploymentId(deployment.getId());
                existingInfo.setProcessDefinitionId(processDefinition.getId());
                existingInfo.setVersion(processDefinition.getVersion());
                existingInfo.setStatus(1); // 激活状态
                // 根据流程 key 更新分类
                if (processDefinition.getKey().contains("businessTrip") || processDefinition.getKey().contains("trip")) {
                    existingInfo.setCategory("business_trip");
                } else if (processDefinition.getKey().contains("pettyCash") || processDefinition.getKey().contains("cash")) {
                    existingInfo.setCategory("petty_cash");
                }
                    definitionInfoMapper.updateById(existingInfo);
                log.info("更新流程定义信息成功，流程KEY：{}，版本：{}", processDefinition.getKey(), processDefinition.getVersion());
            } else {
                // 新建记录 - 使用try-catch处理可能的重复键异常
                log.info("准备插入新记录到process_definition_info表...");
                try {
                    ProcessDefinitionInfo definitionInfo = new ProcessDefinitionInfo();
                    definitionInfo.setProcessKey(processDefinition.getKey());
                    definitionInfo.setProcessName(processName);
                    definitionInfo.setDeploymentId(deployment.getId());
                    definitionInfo.setProcessDefinitionId(processDefinition.getId());
                    definitionInfo.setVersion(processDefinition.getVersion());
                    definitionInfo.setStatus(1);
                    // 根据流程 key 猜测分类
                    if (processDefinition.getKey().contains("businessTrip") || processDefinition.getKey().contains("trip")) {
                        definitionInfo.setCategory("business_trip");
                    } else if (processDefinition.getKey().contains("pettyCash") || processDefinition.getKey().contains("cash")) {
                        definitionInfo.setCategory("petty_cash");
                    }
                    
                    log.info("准备执行insert操作，processKey: {}, processName: {}", 
                            definitionInfo.getProcessKey(), definitionInfo.getProcessName());
                    int result = definitionInfoMapper.insert(definitionInfo);
                    log.info("新增流程定义信息SUCCESS，流程KEY：{}，版本：{}，插入结果：{}，生成ID：{}", 
                            processDefinition.getKey(), processDefinition.getVersion(), result, definitionInfo.getId());
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    // 如果插入时发现重复（并发情况），则查询并更新
                    log.warn("检测到重复的流程KEY（DuplicateKeyException），转为更新操作：{}", processDefinition.getKey());
                    ProcessDefinitionInfo duplicateInfo = definitionInfoMapper.selectOne(
                            new LambdaQueryWrapper<ProcessDefinitionInfo>()
                                    .eq(ProcessDefinitionInfo::getProcessKey, processDefinition.getKey())
                    );
                    if (duplicateInfo != null) {
                        duplicateInfo.setProcessName(processName);
                        duplicateInfo.setDeploymentId(deployment.getId());
                        duplicateInfo.setProcessDefinitionId(processDefinition.getId());
                        duplicateInfo.setVersion(processDefinition.getVersion());
                        duplicateInfo.setStatus(1);
                        if (processDefinition.getKey().contains("businessTrip") || processDefinition.getKey().contains("trip")) {
                            duplicateInfo.setCategory("business_trip");
                        } else if (processDefinition.getKey().contains("pettyCash") || processDefinition.getKey().contains("cash")) {
                            duplicateInfo.setCategory("petty_cash");
                        }
                        definitionInfoMapper.updateById(duplicateInfo);
                        log.info("重复KEY更新成功");
                    }
                } catch (Exception ex) {
                    log.error("插入process_definition_info表失败，异常类型：{}", ex.getClass().getName(), ex);
                    throw ex;
                }
            }
            
            log.info("预置流程部署完成，流程KEY：{}，版本：{}，准备返回部署ID", 
                    processDefinition.getKey(), processDefinition.getVersion());
            return deployment.getId();
        } catch (Exception e) {
            log.error("预置流程部署失败，错误信息：{}", e.getMessage(), e);
            throw new RuntimeException("预置流程部署失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployProcessByDesign(ProcessDeployRequest request) {
        try {
            // 1. 部署BPMN到Flowable引擎
            String bpmnXml = request.getBpmnXml();
            if (StrUtil.isBlank(bpmnXml)) {
                throw new RuntimeException("BPMN XML内容不能为空");
            }
            
            InputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
            String resourceName = request.getProcessKey() + ".bpmn20.xml";
            
            Deployment deployment = repositoryService.createDeployment()
                    .name(request.getProcessName())
                    .addInputStream(resourceName, inputStream)
                    .deploy();
            
            // 2. 获取流程定义信息
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义获取失败");
            }
            
            // 3. 保存或更新流程定义扩展信息
            ProcessDefinitionInfo definitionInfo;
            ProcessDefinitionInfo existingInfo = definitionInfoMapper.selectOne(
                    new LambdaQueryWrapper<ProcessDefinitionInfo>()
                            .eq(ProcessDefinitionInfo::getProcessKey, processDefinition.getKey())
            );
            
            if (existingInfo != null) {
                // 更新现有记录
                definitionInfo = existingInfo;
                definitionInfo.setProcessName(request.getProcessName());
                definitionInfo.setCategory(request.getCategory());
                definitionInfo.setDescription(request.getDescription());
                definitionInfo.setDeploymentId(deployment.getId());
                definitionInfo.setProcessDefinitionId(processDefinition.getId());
                definitionInfo.setVersion(processDefinition.getVersion());
                definitionInfo.setStatus(1); // 激活状态
                definitionInfoMapper.updateById(definitionInfo);
            } else {
                // 新建记录
                definitionInfo = new ProcessDefinitionInfo();
                definitionInfo.setProcessKey(processDefinition.getKey());
                definitionInfo.setProcessName(request.getProcessName());
                definitionInfo.setCategory(request.getCategory());
                definitionInfo.setDescription(request.getDescription());
                definitionInfo.setDeploymentId(deployment.getId());
                definitionInfo.setProcessDefinitionId(processDefinition.getId());
                definitionInfo.setVersion(processDefinition.getVersion());
                definitionInfo.setStatus(1);
                definitionInfoMapper.insert(definitionInfo);
            }
            
            // 4. 保存节点配置
            if (request.getNodes() != null && !request.getNodes().isEmpty()) {
                // 先删除旧的节点配置
                nodeMapper.delete(new LambdaQueryWrapper<ProcessNode>()
                        .eq(ProcessNode::getProcessKey, processDefinition.getKey()));
                
                // 插入新的节点配置
                for (ProcessNodeDTO nodeDTO : request.getNodes()) {
                    ProcessNode node = new ProcessNode();
                    BeanUtils.copyProperties(nodeDTO, node);
                    node.setProcessKey(processDefinition.getKey());
                    nodeMapper.insert(node);
                }
            }
            
            log.info("流程部署成功，流程KEY：{}，版本：{}", processDefinition.getKey(), processDefinition.getVersion());
            return deployment.getId();
            
        } catch (Exception e) {
            log.error("流程部署失败", e);
            throw new RuntimeException("流程部署失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeployment(String deploymentId, boolean cascade) {
        try {
            // 删除部署（cascade为true时会级联删除流程实例）
            repositoryService.deleteDeployment(deploymentId, cascade);
            
            // 删除扩展信息（如果是最后一个部署）
            ProcessDefinitionInfo info = definitionInfoMapper.selectOne(
                    new LambdaQueryWrapper<ProcessDefinitionInfo>()
                            .eq(ProcessDefinitionInfo::getDeploymentId, deploymentId)
            );
            
            if (info != null) {
                String processKey = info.getProcessKey();
                
                // 检查是否还有其他版本
                long count = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey)
                        .count();
                
                if (count == 0) {
                    // 删除流程定义信息和节点配置
                    definitionInfoMapper.deleteById(info.getId());
                    nodeMapper.delete(new LambdaQueryWrapper<ProcessNode>()
                            .eq(ProcessNode::getProcessKey, processKey));
                } else {
                    // 更新为最新版本信息
                    ProcessDefinition latestDef = repositoryService.createProcessDefinitionQuery()
                            .processDefinitionKey(processKey)
                            .latestVersion()
                            .singleResult();
                    
                    if (latestDef != null) {
                        Deployment latestDeployment = repositoryService.createDeploymentQuery()
                                .deploymentId(latestDef.getDeploymentId())
                                .singleResult();
                        
                        info.setDeploymentId(latestDef.getDeploymentId());
                        info.setProcessDefinitionId(latestDef.getId());
                        info.setVersion(latestDef.getVersion());
                        definitionInfoMapper.updateById(info);
                    }
                }
            }
            
            log.info("流程部署删除成功，部署ID：{}", deploymentId);
        } catch (Exception e) {
            log.error("删除流程部署失败", e);
            throw new RuntimeException("删除流程部署失败：" + e.getMessage());
        }
    }
    
    @Override
    public Page<ProcessDefinitionDTO> getProcessDefinitionList(Integer current, Integer size, String category) {
        Page<ProcessDefinitionDTO> resultPage = new Page<>(current, size);
        
        LambdaQueryWrapper<ProcessDefinitionInfo> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(ProcessDefinitionInfo::getCategory, category);
        }
        wrapper.orderByDesc(ProcessDefinitionInfo::getCreateTime);
        
        Page<ProcessDefinitionInfo> infoPage = definitionInfoMapper.selectPage(
                new Page<>(current, size), wrapper);
        
        log.info("查询流程定义列表，总数：{}，当前页：{}，每页大小：{}，分类：{}", 
                infoPage.getTotal(), current, size, category);
        
        List<ProcessDefinitionDTO> dtoList = infoPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        resultPage.setRecords(dtoList);
        resultPage.setTotal(infoPage.getTotal());
        
        return resultPage;
    }
    
    @Override
    public ProcessDefinitionDTO getProcessDefinitionDetail(String processKey) {
        ProcessDefinitionInfo info = definitionInfoMapper.selectOne(
                new LambdaQueryWrapper<ProcessDefinitionInfo>()
                        .eq(ProcessDefinitionInfo::getProcessKey, processKey)
        );
        
        if (info == null) {
            throw new RuntimeException("流程定义不存在");
        }
        
        ProcessDefinitionDTO dto = convertToDTO(info);
        
        // 加载节点配置
        List<ProcessNode> nodes = nodeMapper.selectList(
                new LambdaQueryWrapper<ProcessNode>()
                        .eq(ProcessNode::getProcessKey, processKey)
                        .orderByAsc(ProcessNode::getNodeOrder)
        );
        
        List<ProcessNodeDTO> nodeDTOs = nodes.stream()
                .map(this::convertNodeToDTO)
                .collect(Collectors.toList());
        
        dto.setNodes(nodeDTOs);
        
        return dto;
    }
    
    @Override
    public void activateProcess(String processKey) {
        ProcessDefinitionInfo info = getProcessInfoByKey(processKey);
        
        // 激活流程定义
        repositoryService.activateProcessDefinitionByKey(processKey, true, null);
        
        // 更新状态
        info.setStatus(1);
        definitionInfoMapper.updateById(info);
        
        log.info("流程已激活：{}", processKey);
    }
    
    @Override
    public void suspendProcess(String processKey) {
        ProcessDefinitionInfo info = getProcessInfoByKey(processKey);
        
        // 暂停流程定义
        repositoryService.suspendProcessDefinitionByKey(processKey, true, null);
        
        // 更新状态
        info.setStatus(0);
        definitionInfoMapper.updateById(info);
        
        log.info("流程已暂停：{}", processKey);
    }
    
    @Override
    public String getProcessXml(String processDefinitionId) {
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            if (bpmnModel == null) {
                throw new RuntimeException("流程定义不存在");
            }
            
            BpmnXMLConverter converter = new BpmnXMLConverter();
            byte[] bpmnBytes = converter.convertToXML(bpmnModel);
            
            return new String(bpmnBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("获取流程XML失败", e);
            throw new RuntimeException("获取流程XML失败：" + e.getMessage());
        }
    }
    
    @Override
    public byte[] exportProcessBpmn(String processDefinitionId) {
        try {
            String xml = getProcessXml(processDefinitionId);
            return xml.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("导出流程BPMN失败", e);
            throw new RuntimeException("导出流程BPMN失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<ProcessDefinitionDTO> getProcessVersions(String processKey) {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        
        return definitions.stream()
                .map(def -> {
                    ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
                    dto.setProcessKey(def.getKey());
                    dto.setProcessName(def.getName());
                    dto.setProcessDefinitionId(def.getId());
                    dto.setDeploymentId(def.getDeploymentId());
                    dto.setVersion(def.getVersion());
                    dto.setStatus(def.isSuspended() ? 0 : 1);
                    
                    // 获取部署时间
                    Deployment deployment = repositoryService.createDeploymentQuery()
                            .deploymentId(def.getDeploymentId())
                            .singleResult();
                    if (deployment != null) {
                        dto.setDeployTime(deployment.getDeploymentTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String startProcess(String processKey, String businessKey, Long userId) {
        try {
            // 1. 获取流程定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processKey)
                    .latestVersion()
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义不存在：" + processKey);
            }
            
            if (processDefinition.isSuspended()) {
                throw new RuntimeException("流程已被暂停，无法启动");
            }
            
            // 2. 设置流程变量
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("userId", userId);
            variables.put("businessKey", businessKey);
            
            // 3. 启动流程实例
            org.flowable.engine.runtime.ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(processKey, businessKey, variables);
            
            log.info("流程实例启动成功，流程KEY：{}，实例ID：{}，业务KEY：{}", 
                    processKey, processInstance.getId(), businessKey);
            
            return processInstance.getId();
            
        } catch (Exception e) {
            log.error("启动流程失败", e);
            throw new RuntimeException("启动流程失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, String comment, boolean approved) {
        try {
            // 1. 获取任务信息
            org.flowable.task.api.Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();
            
            if (task == null) {
                throw new RuntimeException("任务不存在：" + taskId);
            }
            
            // 2. 获取当前用户信息
            Long currentUserId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
            SysUser currentUser = sysUserMapper.selectById(currentUserId);
            
            // 3. 获取业务KEY
            org.flowable.engine.runtime.ProcessInstance processInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            
            String businessKey = processInstance.getBusinessKey();
            
            // 4. 保存审批意见
            ApprovalOpinion opinion = new ApprovalOpinion();
            opinion.setBusinessId(Long.parseLong(businessKey));
            opinion.setTaskId(taskId);
            opinion.setApproverId(currentUserId);
            opinion.setApproverName(currentUser.getRealName());
            opinion.setOpinion(comment);
            opinion.setAction(approved ? "APPROVE" : "REJECT");
            
            // 根据流程定义KEY判断业务类型
            String processKey = task.getProcessDefinitionId().split(":")[0];
            if (processKey.contains("businessTrip") || processKey.contains("trip")) {
                opinion.setBusinessType("business_trip");
            } else if (processKey.contains("pettyCash") || processKey.contains("cash")) {
                opinion.setBusinessType("petty_cash");
            }
            
            opinionMapper.insert(opinion);
            
            // 5. 设置流程变量
            java.util.Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("approved", approved);
            variables.put("comment", comment);
            variables.put("approverId", currentUserId);
            
            // 6. 完成任务
            taskService.complete(taskId, variables);
            
            // 7. 检查流程是否结束，更新业务状态
            org.flowable.engine.runtime.ProcessInstance updatedInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            
            if (updatedInstance == null) {
                // 流程已结束，更新业务状态
                updateBusinessStatus(businessKey, processKey, approved);
            }
            
            log.info("任务完成成功，任务ID：{}，审批结果：{}", taskId, approved ? "通过" : "驳回");
            
        } catch (Exception e) {
            log.error("完成任务失败", e);
            throw new RuntimeException("完成任务失败：" + e.getMessage());
        }
    }
    
    @Override
    public Page<TaskVO> getUserTasks(Long userId, Integer current, Integer size) {
        try {
            Page<TaskVO> resultPage = new Page<>(current, size);
            
            // 1. 查询用户待办任务
            org.flowable.task.api.TaskQuery taskQuery = taskService.createTaskQuery()
                    .taskAssignee(userId.toString())
                    .orderByTaskCreateTime()
                    .desc();
            
            // 分页查询
            long total = taskQuery.count();
            int firstResult = (current - 1) * size;
            List<org.flowable.task.api.Task> tasks = taskQuery
                    .listPage(firstResult, size);
            
            // 2. 转换为VO
            List<TaskVO> taskVOList = new java.util.ArrayList<>();
            
            for (org.flowable.task.api.Task task : tasks) {
                TaskVO vo = new TaskVO();
                vo.setTaskId(task.getId());
                vo.setTaskName(task.getName());
                vo.setProcessInstanceId(task.getProcessInstanceId());
                vo.setCreateTime(task.getCreateTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime());
                vo.setPriority(task.getPriority());
                
                // 获取流程实例信息
                org.flowable.engine.runtime.ProcessInstance processInstance = runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();
                
                if (processInstance != null) {
                    String businessKey = processInstance.getBusinessKey();
                    vo.setBusinessKey(businessKey);
                    
                    // 获取流程定义KEY
                    String processDefId = processInstance.getProcessDefinitionId();
                    String processKey = processDefId.split(":")[0];
                    vo.setProcessKey(processKey);
                    
                    // 获取流程名称
                    ProcessDefinitionInfo info = definitionInfoMapper.selectOne(
                            new LambdaQueryWrapper<ProcessDefinitionInfo>()
                                    .eq(ProcessDefinitionInfo::getProcessKey, processKey)
                    );
                    if (info != null) {
                        vo.setProcessName(info.getProcessName());
                    }
                    
                    // 填充业务信息
                    fillBusinessInfo(vo, businessKey, processKey);
                }
                
                taskVOList.add(vo);
            }
            
            resultPage.setRecords(taskVOList);
            resultPage.setTotal(total);
            
            return resultPage;
            
        } catch (Exception e) {
            log.error("查询用户待办任务失败", e);
            throw new RuntimeException("查询待办任务失败：" + e.getMessage());
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 更新业务状态
     */
    private void updateBusinessStatus(String businessKey, String processKey, boolean approved) {
        try {
            Long businessId = Long.parseLong(businessKey);
            int status = approved ? 2 : 3; // 2-已通过，3-已驳回
            
            if (processKey.contains("businessTrip") || processKey.contains("trip")) {
                // 更新出差申请状态
                BusinessTrip trip = businessTripMapper.selectById(businessId);
                if (trip != null) {
                    trip.setStatus(status);
                    businessTripMapper.updateById(trip);
                }
            } else if (processKey.contains("pettyCash") || processKey.contains("cash")) {
                // 更新备用金申请状态
                PettyCash cash = pettyCashMapper.selectById(businessId);
                if (cash != null) {
                    cash.setStatus(status);
                    pettyCashMapper.updateById(cash);
                }
            }
        } catch (Exception e) {
            log.error("更新业务状态失败", e);
        }
    }
    
    /**
     * 填充业务信息
     */
    private void fillBusinessInfo(TaskVO vo, String businessKey, String processKey) {
        try {
            Long businessId = Long.parseLong(businessKey);
            
            if (processKey.contains("businessTrip") || processKey.contains("trip")) {
                vo.setBusinessType("business_trip");
                BusinessTrip trip = businessTripMapper.selectById(businessId);
                if (trip != null) {
                    vo.setApplyNo(trip.getApplyNo());
                    vo.setReason(trip.getReason());
                    vo.setApplicantId(trip.getUserId());
                    
                    // 获取申请人信息
                    SysUser user = sysUserMapper.selectById(trip.getUserId());
                    if (user != null) {
                        vo.setApplicantName(user.getRealName());
                    }
                    
                    // 获取部门信息
                    if (trip.getDeptId() != null) {
                        SysDept dept = sysDeptMapper.selectById(trip.getDeptId());
                        if (dept != null) {
                            vo.setDeptName(dept.getDeptName());
                        }
                    }
                }
            } else if (processKey.contains("pettyCash") || processKey.contains("cash")) {
                vo.setBusinessType("petty_cash");
                PettyCash cash = pettyCashMapper.selectById(businessId);
                if (cash != null) {
                    vo.setApplyNo(cash.getApplyNo());
                    vo.setReason(cash.getReason());
                    vo.setApplicantId(cash.getUserId());
                    
                    // 获取申请人信息
                    SysUser user = sysUserMapper.selectById(cash.getUserId());
                    if (user != null) {
                        vo.setApplicantName(user.getRealName());
                    }
                    
                    // 获取部门信息
                    if (cash.getDeptId() != null) {
                        SysDept dept = sysDeptMapper.selectById(cash.getDeptId());
                        if (dept != null) {
                            vo.setDeptName(dept.getDeptName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("填充业务信息失败", e);
        }
    }
    
    private ProcessDefinitionInfo getProcessInfoByKey(String processKey) {
        ProcessDefinitionInfo info = definitionInfoMapper.selectOne(
                new LambdaQueryWrapper<ProcessDefinitionInfo>()
                        .eq(ProcessDefinitionInfo::getProcessKey, processKey)
        );
        
        if (info == null) {
            throw new RuntimeException("流程定义不存在");
        }
        
        return info;
    }
    
    private ProcessDefinitionDTO convertToDTO(ProcessDefinitionInfo info) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setProcessKey(info.getProcessKey());
        dto.setProcessName(info.getProcessName());
        dto.setCategory(info.getCategory());
        dto.setDescription(info.getDescription());
        dto.setDeploymentId(info.getDeploymentId());
        dto.setProcessDefinitionId(info.getProcessDefinitionId());
        dto.setVersion(info.getVersion());
        dto.setStatus(info.getStatus());
        
        // 从 Flowable Deployment 表中获取实际部署时间
        try {
            if (info.getDeploymentId() != null) {
                Deployment deployment = repositoryService.createDeploymentQuery()
                        .deploymentId(info.getDeploymentId())
                        .singleResult();
                if (deployment != null && deployment.getDeploymentTime() != null) {
                    dto.setDeployTime(deployment.getDeploymentTime().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime());
                } else {
                    dto.setDeployTime(info.getCreateTime());
                }
            } else {
                dto.setDeployTime(info.getCreateTime());
            }
        } catch (Exception e) {
            log.warn("获取部署时间失败，使用创建时间：{}", info.getProcessKey());
            dto.setDeployTime(info.getCreateTime());
        }
        
        return dto;
    }
    
    private ProcessNodeDTO convertNodeToDTO(ProcessNode node) {
        ProcessNodeDTO dto = new ProcessNodeDTO();
        BeanUtils.copyProperties(node, dto);
        return dto;
    }
}
