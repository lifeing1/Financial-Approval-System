package com.approval.service;

import com.approval.dto.BusinessTripDTO;
import com.approval.entity.BusinessTrip;
import com.approval.vo.BusinessTripVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 出差申请服务接口
 */
public interface BusinessTripService {
    
    /**
     * 保存草稿
     */
    Long saveDraft(BusinessTripDTO dto);
    
    /**
     * 提交申请
     */
    void submitApply(Long id);
    
    /**
     * 查询我的申请列表
     */
    IPage<BusinessTrip> getMyApplyList(Page<BusinessTrip> page, Integer status);
    
    /**
     * 查询待办列表
     */
    IPage<BusinessTrip> getTodoList(Page<BusinessTrip> page);
    
    /**
     * 查询已办列表
     */
    IPage<BusinessTrip> getDoneList(Page<BusinessTrip> page);
    
    /**
     * 查询详情
     */
    BusinessTrip getDetail(Long id);
    
    /**
     * 查询详情（包含费用明细和附件）
     */
    BusinessTripVO getDetailWithExpenses(Long id);
    
    /**
     * 审批
     */
    void approve(Long id, String opinion);
    
    /**
     * 驳回
     */
    void reject(Long id, String opinion);
}
