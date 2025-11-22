package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.dto.BusinessTripDTO;
import com.approval.dto.TripExpenseDTO;
import com.approval.entity.BusinessTrip;
import com.approval.entity.SysUser;
import com.approval.entity.TripExpense;
import com.approval.mapper.BusinessTripMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.mapper.TripExpenseMapper;
import com.approval.service.BusinessTripService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 出差申请服务实现
 */
@Service
@RequiredArgsConstructor
public class BusinessTripServiceImpl implements BusinessTripService {
    
    private final BusinessTripMapper businessTripMapper;
    private final TripExpenseMapper tripExpenseMapper;
    private final SysUserMapper userMapper;
    
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
            // 删除旧的费用明细
            tripExpenseMapper.delete(new LambdaQueryWrapper<TripExpense>()
                    .eq(TripExpense::getTripId, trip.getId()));
        }
        
        // 保存费用明细
        if (dto.getExpenses() != null && !dto.getExpenses().isEmpty()) {
            for (TripExpenseDTO expenseDTO : dto.getExpenses()) {
                TripExpense expense = new TripExpense();
                expense.setTripId(trip.getId());
                expense.setExpenseType(expenseDTO.getExpenseType());
                expense.setAmount(expenseDTO.getAmount());
                expense.setRemark(expenseDTO.getRemark());
                expense.setAttachments(expenseDTO.getAttachments());
                tripExpenseMapper.insert(expense);
            }
        }
        
        return trip.getId();
    }
    
    @Override
    public void submitApply(Long id) {
        BusinessTrip trip = businessTripMapper.selectById(id);
        if (trip == null) {
            throw new BusinessException("申请不存在");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        if (trip.getStatus() != 0) {
            throw new BusinessException("只有草稿状态才能提交");
        }
        
        // TODO: 启动工作流
        trip.setStatus(1); // 审批中
        businessTripMapper.updateById(trip);
    }
    
    @Override
    public IPage<BusinessTrip> getMyApplyList(Page<BusinessTrip> page, Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<BusinessTrip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BusinessTrip::getUserId, userId);
        if (status != null) {
            wrapper.eq(BusinessTrip::getStatus, status);
        }
        wrapper.orderByDesc(BusinessTrip::getCreateTime);
        return businessTripMapper.selectPage(page, wrapper);
    }
    
    @Override
    public IPage<BusinessTrip> getTodoList(Page<BusinessTrip> page) {
        // TODO: 查询当前用户的待办任务
        return page;
    }
    
    @Override
    public IPage<BusinessTrip> getDoneList(Page<BusinessTrip> page) {
        // TODO: 查询当前用户的已办任务
        return page;
    }
    
    @Override
    public BusinessTrip getDetail(Long id) {
        return businessTripMapper.selectById(id);
    }
    
    @Override
    public void approve(Long id, String opinion) {
        // TODO: 完成审批操作
        BusinessTrip trip = businessTripMapper.selectById(id);
        if (trip == null) {
            throw new BusinessException("申请不存在");
        }
    }
    
    @Override
    public void reject(Long id, String opinion) {
        // TODO: 完成驳回操作
        BusinessTrip trip = businessTripMapper.selectById(id);
        if (trip == null) {
            throw new BusinessException("申请不存在");
        }
        trip.setStatus(3); // 已驳回
        businessTripMapper.updateById(trip);
    }
}
