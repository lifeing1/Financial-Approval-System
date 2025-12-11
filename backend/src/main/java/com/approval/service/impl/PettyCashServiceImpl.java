package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.dto.PettyCashDTO;
import com.approval.entity.ApprovalOpinion;
import com.approval.entity.PettyCash;
import com.approval.entity.SysUser;
import com.approval.entity.SysDept;
import com.approval.mapper.*;
import com.approval.service.PettyCashService;
import com.approval.service.WorkflowService;
import com.approval.vo.PettyCashVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 备用金申请服务实现
 */
@Service
@RequiredArgsConstructor
public class PettyCashServiceImpl implements PettyCashService {
    
    private final PettyCashMapper pettyCashMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper sysDeptMapper;
    private final WorkflowService workflowService;
    private final ApprovalOpinionMapper approvalOpinionMapper;
    private final SysRoleMapper roleMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(PettyCashDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        
        PettyCash pettyCash = new PettyCash();
        pettyCash.setId(dto.getId());
        pettyCash.setUserId(userId);
        pettyCash.setDeptId(user.getDeptId());
        pettyCash.setApplyDate(LocalDate.now());
        pettyCash.setReason(dto.getReason());
        pettyCash.setAmount(dto.getAmount());
        pettyCash.setUsePeriod(dto.getUsePeriod());
        pettyCash.setRepaymentPlan(dto.getRepaymentPlan());
        pettyCash.setRemark(dto.getRemark());
        pettyCash.setStatus(0); // 草稿
        
        if (pettyCash.getId() == null) {
            // 生成申请编号
            pettyCash.setApplyNo("PC" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + System.currentTimeMillis() % 10000);
            pettyCash.setCreateTime(LocalDateTime.now());
            pettyCash.setUpdateTime(LocalDateTime.now());
            pettyCashMapper.insert(pettyCash);
        } else {
            pettyCash.setUpdateTime(LocalDateTime.now());
            pettyCashMapper.updateById(pettyCash);
        }
        
        return pettyCash.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApply(Long id) {
        PettyCash pettyCash = pettyCashMapper.selectById(id);
        
        if (pettyCash == null) {
            throw new BusinessException("申请不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!pettyCash.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (pettyCash.getStatus() != 0) {
            throw new BusinessException("只有草稿状态才能提交");
        }
        
        // 启动工作流（如果失败，事务会自动回滚）
        try {
            String processKey = "pettyCashProcess"; // 备用金申请流程定义KEY
            String businessKey = pettyCash.getId().toString();
            String processInstanceId = workflowService.startProcess(processKey, businessKey, userId, pettyCash.getDeptId());
            
            // 更新流程实例 ID 和状态
            pettyCash.setProcessInstanceId(processInstanceId);
            pettyCash.setStatus(1); // 审批中
            pettyCash.setUpdateTime(LocalDateTime.now());
            pettyCashMapper.updateById(pettyCash);
        } catch (Exception e) {
            throw new BusinessException("启动审批流程失败：" + e.getMessage());
        }
    }
    
    @Override
    public IPage<PettyCash> getMyApplyList(Page<PettyCash> page, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<PettyCash> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PettyCash::getUserId, userId);
        if (status != null) {
            wrapper.eq(PettyCash::getStatus, status);
        }
        wrapper.orderByDesc(PettyCash::getCreateTime);
        return pettyCashMapper.selectPage(page, wrapper);
    }
    
    @Override
    public IPage<com.approval.vo.TaskVO> getTodoList(Page<PettyCash> page) {
        // 查询当前用户的待办任务
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 调用工作流服务获取待办任务
        Page<com.approval.vo.TaskVO> taskPage = workflowService.getUserTasks(userId, 
                (int) page.getCurrent(), 
                (int) page.getSize());
        
        // 过滤出备用金申请的任务
        java.util.List<com.approval.vo.TaskVO> pettyCashTasks = taskPage.getRecords().stream()
                .filter(task -> "petty_cash".equals(task.getBusinessType()))
                .collect(java.util.stream.Collectors.toList());
        
        // 构建返回结果
        Page<com.approval.vo.TaskVO> resultPage = new Page<>(page.getCurrent(), page.getSize());
        resultPage.setRecords(pettyCashTasks);
        resultPage.setTotal(pettyCashTasks.size());
        
        return resultPage;
    }
    
    @Override
    public IPage<com.approval.vo.TaskVO> getDoneList(Page<PettyCash> page) {
        // 查询当前用户的已办任务
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 调用工作流服务获取已办任务
        Page<com.approval.vo.TaskVO> taskPage = workflowService.getUserHistoryTasks(userId, 
                (int) page.getCurrent(), 
                (int) page.getSize());
        
        // 过滤出备用金申请的任务
        java.util.List<com.approval.vo.TaskVO> pettyCashTasks = taskPage.getRecords().stream()
                .filter(task -> "petty_cash".equals(task.getBusinessType()))
                .collect(java.util.stream.Collectors.toList());

        //审批结果赋值
        pettyCashTasks.forEach(task -> {task.setStatus(pettyCashMapper.selectById(task.getId()).getStatus());});

        //每个审批人的审批时间
        pettyCashTasks.forEach(task -> {task.setApproveTime(approvalOpinionMapper.selectOne(new QueryWrapper<ApprovalOpinion>().eq("task_id", task.getTaskId())).getCreateTime());});
        // 构建返回结果
        Page<com.approval.vo.TaskVO> resultPage = new Page<>(page.getCurrent(), page.getSize());
        resultPage.setRecords(pettyCashTasks);
        resultPage.setTotal(pettyCashTasks.size());
        
        return resultPage;
    }
    
    @Override
    public PettyCash getDetail(Long id) {
        return pettyCashMapper.selectById(id);
    }
    
    @Override
    public PettyCashVO getDetailWithUserInfo(Long id) {
        // 查询备用金申请基本信息
        PettyCash pettyCash = pettyCashMapper.selectById(id);
        if (pettyCash == null) {
            throw new BusinessException("申请不存在");
        }
        
        // 转换为VO
        PettyCashVO vo = new PettyCashVO();
        BeanUtils.copyProperties(pettyCash, vo);
        
        // 查询申请人信息
        SysUser user = userMapper.selectById(pettyCash.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
        }
        
        // 查询部门信息
        if (pettyCash.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(pettyCash.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        String roleName = roleMapper.selectRolesByUserId(StpUtil.getLoginIdAsLong()).get(0).getRoleName();
        if ("普通员工".equals(roleName)) {
            //普通员工不对审批时间做处理
        }else {
            //审批时间处理
            LocalDateTime approvalTime = approvalOpinionMapper.selectOne(new QueryWrapper<ApprovalOpinion>()
                    .eq("business_id", vo.getId())
                    .eq("business_type", "petty_cash")
                    .eq("approver_id", StpUtil.getLoginIdAsLong())).getCreateTime();
            //createTime被占用，用updateTime接收审批时间
            vo.setUpdateTime(approvalTime);
        }

        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(String taskId, String opinion) {
        // 完成审批操作
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BusinessException("任务ID不能为空");
        }
        
        try {
            // 调用工作流服务完成任务
            workflowService.completeTask(taskId, opinion, true);
        } catch (Exception e) {
            throw new BusinessException("审批失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String taskId, String opinion) {
        // 完成驳回操作
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BusinessException("任务ID不能为空");
        }
        
        try {
            // 调用工作流服务完成任务（驳回）
            workflowService.completeTask(taskId, opinion, false);
        } catch (Exception e) {
            throw new BusinessException("驳回失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<com.approval.entity.ApprovalOpinion> getApprovalHistory(Long id) {
        // 查询审批意见列表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.approval.entity.ApprovalOpinion> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.approval.entity.ApprovalOpinion::getBusinessId, id)
               .eq(com.approval.entity.ApprovalOpinion::getBusinessType, "petty_cash")
               .orderByAsc(com.approval.entity.ApprovalOpinion::getCreateTime);
        
        return approvalOpinionMapper.selectList(wrapper);
    }
}
