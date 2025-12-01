package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.dto.AttachmentDTO;
import com.approval.dto.BusinessTripDTO;
import com.approval.dto.TripExpenseDTO;
import com.approval.entity.Attachment;
import com.approval.entity.BusinessTrip;
import com.approval.entity.SysDept;
import com.approval.entity.SysUser;
import com.approval.entity.TripExpense;
import com.approval.mapper.AttachmentMapper;
import com.approval.mapper.BusinessTripMapper;
import com.approval.mapper.SysDeptMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.mapper.TripExpenseMapper;
import com.approval.service.BusinessTripService;
import com.approval.service.WorkflowService;
import com.approval.vo.AttachmentVO;
import com.approval.vo.BusinessTripVO;
import com.approval.vo.TaskVO;
import com.approval.vo.TripExpenseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 出差申请服务实现
 */
@Service
@RequiredArgsConstructor
public class BusinessTripServiceImpl implements BusinessTripService {
    
    private final BusinessTripMapper businessTripMapper;
    private final TripExpenseMapper tripExpenseMapper;
    private final AttachmentMapper attachmentMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper sysDeptMapper;
    private final WorkflowService workflowService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(BusinessTripDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        
        BusinessTrip trip = new BusinessTrip();
        trip.setId(dto.getId());
        trip.setUserId(userId);
        trip.setDeptId(user.getDeptId());
        trip.setApplyDate(LocalDate.now());
        trip.setReason(dto.getReason());
        trip.setStartPlace(dto.getStartPlace());
        trip.setDestination(dto.getDestination());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setTransportModes(dto.getTransportModes());
        trip.setRemark(dto.getRemark());
        trip.setStatus(0); // 草稿
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            for (TripExpenseDTO expense : dto.getExpenses()) {
                totalAmount = totalAmount.add(expense.getAmount());
            }
        }
        trip.setTotalAmount(totalAmount);
        
        if (trip.getId() == null) {
            // 生成申请编号
            trip.setApplyNo("BT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + System.currentTimeMillis() % 10000);
            trip.setCreateTime(LocalDateTime.now());
            trip.setUpdateTime(LocalDateTime.now());
            businessTripMapper.insert(trip);
        } else {
            trip.setUpdateTime(LocalDateTime.now());
            businessTripMapper.updateById(trip);
            // 删除旧的费用明细和关联的附件
            List<TripExpense> oldExpenses = tripExpenseMapper.selectList(
                    new LambdaQueryWrapper<TripExpense>().eq(TripExpense::getTripId, trip.getId()));
            if (!CollectionUtils.isEmpty(oldExpenses)) {
                // 删除费用明细关联的附件
                for (TripExpense expense : oldExpenses) {
                    attachmentMapper.delete(new LambdaQueryWrapper<Attachment>()
                            .eq(Attachment::getBusinessId, expense.getId())
                            .eq(Attachment::getBusinessType, "trip_expense"));
                }
                // 删除费用明细
                tripExpenseMapper.delete(new LambdaQueryWrapper<TripExpense>()
                        .eq(TripExpense::getTripId, trip.getId()));
            }
        }
        
        // 保存费用明细和附件
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            Long userIds = StpUtil.getLoginIdAsLong();
            for (TripExpenseDTO expenseDTO : dto.getExpenses()) {
                // 保存费用明细
                TripExpense expense = new TripExpense();
                expense.setTripId(trip.getId());
                expense.setExpenseType(expenseDTO.getExpenseType());
                expense.setAmount(expenseDTO.getAmount());
                expense.setRemark(expenseDTO.getRemark());
                tripExpenseMapper.insert(expense);
                
                // 保存费用明细的附件
                if (!CollectionUtils.isEmpty(expenseDTO.getAttachments())) {
                    for (AttachmentDTO attachmentDTO : expenseDTO.getAttachments()) {
                        Attachment attachment = new Attachment();
                        attachment.setBusinessId(expense.getId());
                        attachment.setBusinessType("trip_expense");
                        attachment.setFilePath(attachmentDTO.getUrl());
                        attachment.setFileName(attachmentDTO.getFileName());
                        attachment.setUploadUserId(userIds);
                        attachmentMapper.insert(attachment);
                    }
                }
            }
        }
        
        return trip.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApply(BusinessTripDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果传入了ID，检查权限
        if (dto.getId() != null) {
            BusinessTrip existingTrip = businessTripMapper.selectById(dto.getId());
            if (existingTrip != null && !existingTrip.getUserId().equals(userId)) {
                throw new BusinessException("无权操作");
            }
        }
        
        // 创建出差申请对象
        BusinessTrip trip = new BusinessTrip();
        trip.setUserId(userId);
        trip.setDeptId(user.getDeptId());
        trip.setApplyDate(LocalDate.now());
        trip.setReason(dto.getReason());
        trip.setStartPlace(dto.getStartPlace());
        trip.setDestination(dto.getDestination());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setTransportModes(dto.getTransportModes());
        trip.setRemark(dto.getRemark());
        trip.setStatus(1); // 审批中
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            for (TripExpenseDTO expense : dto.getExpenses()) {
                totalAmount = totalAmount.add(expense.getAmount());
            }
        }
        trip.setTotalAmount(totalAmount);
        
        // 生成申请编号
        trip.setApplyNo("BT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + System.currentTimeMillis() % 10000);
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        
        // 保存出差申请
        businessTripMapper.insert(trip);
        
        // 保存费用明细和附件
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            for (TripExpenseDTO expenseDTO : dto.getExpenses()) {
                // 保存费用明细
                TripExpense expense = new TripExpense();
                expense.setTripId(trip.getId());
                expense.setExpenseType(expenseDTO.getExpenseType());
                expense.setAmount(expenseDTO.getAmount());
                expense.setRemark(expenseDTO.getRemark());
                tripExpenseMapper.insert(expense);
                
                // 保存费用明细的附件
                if (!CollectionUtils.isEmpty(expenseDTO.getAttachments())) {
                    for (AttachmentDTO attachmentDTO : expenseDTO.getAttachments()) {
                        Attachment attachment = new Attachment();
                        attachment.setBusinessId(expense.getId());
                        attachment.setBusinessType("trip_expense");
                        attachment.setFilePath(attachmentDTO.getUrl());
                        attachment.setFileName(attachmentDTO.getFileName());
                        attachment.setUploadUserId(userId);
                        attachmentMapper.insert(attachment);
                    }
                }
            }
        }
        
        // 启动工作流（如果失败，事务会自动回滚，不会保存数据）
        try {
            String processKey = "businessTripProcess"; // 出差申请流程定义KEY
            String businessKey = trip.getId().toString();
            String processInstanceId = workflowService.startProcess(processKey, businessKey, userId, trip.getDeptId());
            
            // 更新流程实例ID
            trip.setProcessInstanceId(processInstanceId);
            businessTripMapper.updateById(trip);
        } catch (Exception e) {
            throw new BusinessException("启动审批流程失败：" + e.getMessage());
        }
    }
    
    @Override
    public IPage<BusinessTripVO> getMyApplyList(Page<BusinessTrip> page, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<BusinessTrip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessTrip::getUserId, userId);
        if (status != null) {
            wrapper.eq(BusinessTrip::getStatus, status);
        }
        wrapper.orderByDesc(BusinessTrip::getCreateTime);
        
        IPage<BusinessTrip> tripPage = businessTripMapper.selectPage(page, wrapper);
        
        // 转换为VO，填充用户名和部门名
        IPage<BusinessTripVO> voPage = new Page<>(tripPage.getCurrent(), tripPage.getSize(), tripPage.getTotal());
        List<BusinessTripVO> voList = new ArrayList<>();
        
        for (BusinessTrip trip : tripPage.getRecords()) {
            BusinessTripVO vo = new BusinessTripVO();
            BeanUtils.copyProperties(trip, vo);
            
            // 查询用户信息
            SysUser user = userMapper.selectById(trip.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
            }
            
            // 查询部门信息
            if (trip.getDeptId() != null) {
                SysDept dept = sysDeptMapper.selectById(trip.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
            
            voList.add(vo);
        }
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public IPage<TaskVO> getTodoList(Page<BusinessTrip> page) {
        // 查询当前用户的待办任务
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 调用工作流服务获取待办任务
        Page<TaskVO> taskPage = workflowService.getUserTasks(userId, 
                (int) page.getCurrent(), 
                (int) page.getSize());
        
        // 过滤出差申请的任务
        List<TaskVO> businessTripTasks = taskPage.getRecords().stream()
                .filter(task -> "business_trip".equals(task.getBusinessType()))
                .collect(java.util.stream.Collectors.toList());
        
        // 构建返回结果
        Page<TaskVO> resultPage = new Page<>(page.getCurrent(), page.getSize());
        resultPage.setRecords(businessTripTasks);
        resultPage.setTotal(businessTripTasks.size());
        
        return resultPage;
    }
    
    @Override
    public IPage<TaskVO> getDoneList(Page<BusinessTrip> page) {
        // 查询当前用户的已办任务
        // 注：这里需要从工作流历史中查询，暂时返回空列表
        // 完整实现需要Flowable的HistoryService
        Page<TaskVO> resultPage = new Page<>(page.getCurrent(), page.getSize());
        resultPage.setRecords(new ArrayList<>());
        resultPage.setTotal(0);
        
        return resultPage;
    }
    
    @Override
    public BusinessTrip getDetail(Long id) {
        return businessTripMapper.selectById(id);
    }
    
    @Override
    public BusinessTripVO getDetailWithExpenses(Long id) {
        // 查询出差申请基本信息
        BusinessTrip trip = businessTripMapper.selectById(id);
        if (trip == null) {
            throw new BusinessException("申请不存在");
        }
        
        // 转换为VO
        BusinessTripVO vo = new BusinessTripVO();
        BeanUtils.copyProperties(trip, vo);
        
        // 查询申请人信息
        SysUser user = userMapper.selectById(trip.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
        }
        
        // 查询费用明细
        List<TripExpense> expenses = tripExpenseMapper.selectList(
                new LambdaQueryWrapper<TripExpense>().eq(TripExpense::getTripId, id));
        
        if (!CollectionUtils.isEmpty(expenses)) {
            List<TripExpenseVO> expenseVOList = new ArrayList<>();
            for (TripExpense expense : expenses) {
                TripExpenseVO expenseVO = new TripExpenseVO();
                BeanUtils.copyProperties(expense, expenseVO);
                
                // 查询费用明细的附件
                List<Attachment> attachments = attachmentMapper.selectList(
                        new LambdaQueryWrapper<Attachment>()
                                .eq(Attachment::getBusinessId, expense.getId())
                                .eq(Attachment::getBusinessType, "trip_expense"));
                
                if (!CollectionUtils.isEmpty(attachments)) {
                    List<AttachmentVO> attachmentVOList = new ArrayList<>();
                    for (Attachment attachment : attachments) {
                        AttachmentVO attachmentVO = new AttachmentVO();
                        BeanUtils.copyProperties(attachment, attachmentVO);
                        attachmentVOList.add(attachmentVO);
                    }
                    expenseVO.setAttachments(attachmentVOList);
                }
                
                expenseVOList.add(expenseVO);
            }
            vo.setExpenses(expenseVOList);
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
}
