package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.dto.PettyCashDTO;
import com.approval.entity.PettyCash;
import com.approval.entity.SysUser;
import com.approval.mapper.PettyCashMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.service.PettyCashService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 备用金申请服务实现
 */
@Service
@RequiredArgsConstructor
public class PettyCashServiceImpl implements PettyCashService {
    
    private final PettyCashMapper pettyCashMapper;
    private final SysUserMapper userMapper;
    
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
            pettyCashMapper.insert(pettyCash);
        } else {
            pettyCashMapper.updateById(pettyCash);
        }
        
        return pettyCash.getId();
    }
    
    @Override
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
        
        // TODO: 启动工作流
        pettyCash.setStatus(1); // 审批中
        pettyCashMapper.updateById(pettyCash);
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
    public IPage<PettyCash> getTodoList(Page<PettyCash> page) {
        // TODO: 查询当前用户的待办任务
        return page;
    }
    
    @Override
    public IPage<PettyCash> getDoneList(Page<PettyCash> page) {
        // TODO: 查询当前用户的已办任务
        return page;
    }
    
    @Override
    public PettyCash getDetail(Long id) {
        return pettyCashMapper.selectById(id);
    }
    
    @Override
    public void approve(Long id, String opinion) {
        // TODO: 完成审批操作
        PettyCash pettyCash = pettyCashMapper.selectById(id);
        if (pettyCash == null) {
            throw new BusinessException("申请不存在");
        }
    }
    
    @Override
    public void reject(Long id, String opinion) {
        // TODO: 完成驳回操作
        PettyCash pettyCash = pettyCashMapper.selectById(id);
        if (pettyCash == null) {
            throw new BusinessException("申请不存在");
        }
        pettyCash.setStatus(3); // 已驳回
        pettyCashMapper.updateById(pettyCash);
    }
}
