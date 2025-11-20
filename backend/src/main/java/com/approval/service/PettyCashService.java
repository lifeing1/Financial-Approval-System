package com.approval.service;

import com.approval.dto.PettyCashDTO;
import com.approval.entity.PettyCash;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 备用金申请服务接口
 */
public interface PettyCashService {
    
    /**
     * 保存草稿
     */
    Long saveDraft(PettyCashDTO dto);
    
    /**
     * 提交申请
     */
    void submitApply(Long id);
    
    /**
     * 查询我的申请列表
     */
    IPage<PettyCash> getMyApplyList(Page<PettyCash> page, Integer status);
    
    /**
     * 查询待办列表
     */
    IPage<PettyCash> getTodoList(Page<PettyCash> page);
    
    /**
     * 查询已办列表
     */
    IPage<PettyCash> getDoneList(Page<PettyCash> page);
    
    /**
     * 查询详情
     */
    PettyCash getDetail(Long id);
    
    /**
     * 审批
     */
    void approve(Long id, String opinion);
    
    /**
     * 驳回
     */
    void reject(Long id, String opinion);
}
