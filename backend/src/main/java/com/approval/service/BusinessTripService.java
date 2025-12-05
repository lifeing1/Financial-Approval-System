package com.approval.service;

import com.approval.entity.BusinessTrip;
import com.approval.vo.BusinessTripVO;
import com.approval.vo.TaskVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 出差申请服务接口
 */
public interface BusinessTripService {
    
    /**
     * 保存草稿
     */
    Long saveDraft(BusinessTripVO vo);
    
    /**
     * 提交申请
     */
    void submitApply(Long id);
    
    /**
     * 获取我的申请列表
     */
    IPage<BusinessTripVO> getMyApplyList(Page<BusinessTrip> page, Integer status);
    
    /**
     * 获取待办列表
     */
    IPage<TaskVO> getTodoList(Page<BusinessTrip> page);
    
    /**
     * 获取已办列表
     */
    IPage<TaskVO> getDoneList(Page<BusinessTrip> page);
    
    /**
     * 查询详情（包含费用明细）
     */
    BusinessTripVO getDetailWithExpenses(Long id);
    
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