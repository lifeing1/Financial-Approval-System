package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.dto.BusinessTripDTO;
import com.approval.service.BusinessTripService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 出差审批控制器
 */
@Tag(name = "出差审批管理")
@RestController
@RequestMapping("/api/business-trip")
@RequiredArgsConstructor
public class BusinessTripController {
    
    private final BusinessTripService businessTripService;
    
    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public Result<?> saveDraft(@Valid @RequestBody BusinessTripDTO dto) {
        Long id = businessTripService.saveDraft(dto);
        return Result.success(id);
    }
    
    /**
     * 提交申请
     */
    @Operation(summary = "提交申请")
    @PostMapping("/submit")
    public Result<?> submit(@Valid @RequestBody BusinessTripDTO dto) {
        businessTripService.submitApply(dto);
        return Result.success("提交成功");
    }
    
    /**
     * 我的申请列表
     */
    @Operation(summary = "我的申请列表")
    @GetMapping("/my")
    public Result<?> getMyList(@RequestParam(defaultValue = "1") Integer current,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) Integer status) {
        Page<com.approval.entity.BusinessTrip> page = new Page<>(current, size);
        return Result.success(businessTripService.getMyApplyList(page, status));
    }
    
    /**
     * 待办列表
     */
    @Operation(summary = "待办列表")
    @GetMapping("/todo")
    public Result<?> getTodoList(@RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<com.approval.entity.BusinessTrip> page = new Page<>(current, size);
        return Result.success(businessTripService.getTodoList(page));
    }
    
    /**
     * 已办列表
     */
    @Operation(summary = "已办列表")
    @GetMapping("/done")
    public Result<?> getDoneList(@RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<com.approval.entity.BusinessTrip> page = new Page<>(current, size);
        return Result.success(businessTripService.getDoneList(page));
    }
    
    /**
     * 查询详情
     */
    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(businessTripService.getDetailWithExpenses(id));
    }
    
    /**
     * 审批通过
     */
    @Operation(summary = "审批通过")
    @PostMapping("/approve/{taskId}")
    public Result<?> approve(@PathVariable String taskId, @RequestParam String opinion) {
        businessTripService.approve(taskId, opinion);
        return Result.success("审批成功");
    }
    
    /**
     * 审批驳回
     */
    @Operation(summary = "审批驳回")
    @PostMapping("/reject/{taskId}")
    public Result<?> reject(@PathVariable String taskId, @RequestParam String opinion) {
        businessTripService.reject(taskId, opinion);
        return Result.success("驳回成功");
    }
}
