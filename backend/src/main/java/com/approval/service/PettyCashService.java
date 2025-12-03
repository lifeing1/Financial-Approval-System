package com.approval.service;

import com.approval.dto.PettyCashDTO;
import com.approval.entity.PettyCash;
import com.approval.vo.PettyCashVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

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
     * 获取我的申请列表
     */
    IPage<PettyCash> getMyApplyList(Page<PettyCash> page, Integer status);
    
    /**
     * 获取待办列表
     */
    IPage<com.approval.vo.TaskVO> getTodoList(Page<PettyCash> page);
    
    /**
     * 获取已办列表
     */
    IPage<com.approval.vo.TaskVO> getDoneList(Page<PettyCash> page);
    
    /**
     * 查询详情
     */
    PettyCash getDetail(Long id);
    
    /**
     * 查询详情（包含用户和部门信息）
     */
    PettyCashVO getDetailWithUserInfo(Long id);
    
    /**
     * 审批通过
     */
    void approve(String taskId, String opinion);
    
    /**
     * 审批驳回
     */
    void reject(String taskId, String opinion);
    
    /**
     * 获取审批历史记录
     */
    List<com.approval.entity.ApprovalOpinion> getApprovalHistory(Long id);
}