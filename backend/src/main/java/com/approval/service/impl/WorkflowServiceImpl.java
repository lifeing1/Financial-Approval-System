package com.approval.service.impl;

import cn.hutool.core.util.StrUtil;
import com.approval.dto.ProcessDefinitionDTO;
import com.approval.dto.ProcessDeployRequest;
import com.approval.dto.ProcessNodeDTO;
import com.approval.entity.ProcessDefinitionInfo;
import com.approval.entity.ProcessNode;
import com.approval.mapper.ProcessDefinitionInfoMapper;
import com.approval.mapper.ProcessNodeMapper;
import com.approval.service.WorkflowService;
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
        try {
            // 1. 从 classpath 加载 BPMN 文件
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:processes/" + fileName);
            
            if (!resource.exists()) {
                throw new RuntimeException("BPMN文件不存在：" + fileName);
            }
            
            InputStream inputStream = resource.getInputStream();
            
            // 2. 部署到 Flowable 引擎
            Deployment deployment = repositoryService.createDeployment()
                    .name(processName)
                    .addInputStream(fileName, inputStream)
                    .deploy();
            
            // 3. 获取流程定义信息
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            
            if (processDefinition == null) {
                throw new RuntimeException("流程定义获取失败");
            }
            
            // 4. 保存或更新流程定义扩展信息
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
            
            log.info("预置流程部署成功，流程KEY：{}，版本：{}", processDefinition.getKey(), processDefinition.getVersion());
            return deployment.getId();
        } catch (Exception e) {
            log.error("预置流程部署失败", e);
            throw new RuntimeException("预置流程部署失败：" + e.getMessage());
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
        // TODO: 实现流程启动逻辑
        return null;
    }
    
    @Override
    public void completeTask(String taskId, String comment, boolean approved) {
        // TODO: 实现任务完成逻辑
    }
    
    @Override
    public Page<?> getUserTasks(Long userId, Integer current, Integer size) {
        // TODO: 实现获取用户待办任务
        return null;
    }
    
    // ==================== 私有辅助方法 ====================
    
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
        dto.setDeployTime(info.getCreateTime());
        
        return dto;
    }
    
    private ProcessNodeDTO convertNodeToDTO(ProcessNode node) {
        ProcessNodeDTO dto = new ProcessNodeDTO();
        BeanUtils.copyProperties(node, dto);
        return dto;
    }
}
