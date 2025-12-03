package com.approval.controller;

import com.approval.common.result.Result;
import com.approval.dto.PettyCashDTO;
import com.approval.service.PettyCashService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 备用金审批控制器
 */
@Tag(name = "备用金审批管理")
@RestController
@RequestMapping("/api/petty-cash")
@RequiredArgsConstructor
public class PettyCashController {
    
    private final PettyCashService pettyCashService;
    
    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public Result<?> saveDraft(@Valid @RequestBody PettyCashDTO dto) {
        Long id = pettyCashService.saveDraft(dto);
        return Result.success(id);
    }
    
    /**
     * 提交申请
     */
    @Operation(summary = "提交申请")
    @PostMapping("/submit/{id}")
    public Result<?> submit(@PathVariable Long id) {
        pettyCashService.submitApply(id);
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
        Page<com.approval.entity.PettyCash> page = new Page<>(current, size);
        return Result.success(pettyCashService.getMyApplyList(page, status));
    }
    
    /**
     * 待办列表
     */
    @Operation(summary = "待办列表")
    @GetMapping("/todo")
    public Result<?> getTodoList(@RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<com.approval.entity.PettyCash> page = new Page<>(current, size);
        return Result.success(pettyCashService.getTodoList(page));
    }
    
    /**
     * 已办列表
     */
    @Operation(summary = "已办列表")
    @GetMapping("/done")
    public Result<?> getDoneList(@RequestParam(defaultValue = "1") Integer current,
                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<com.approval.entity.PettyCash> page = new Page<>(current, size);
        return Result.success(pettyCashService.getDoneList(page));
    }
    
    /**
     * 查询详情
     */
    @Operation(summary = "查询详情")
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return Result.success(pettyCashService.getDetail(id));
    }
    
    /**
     * 查询详情（包含用户和部门信息）
     */
    @Operation(summary = "查询详情（包含用户和部门信息）")
    @GetMapping("/detail/{id}")
    public Result<?> getDetailWithUserInfo(@PathVariable Long id) {
        return Result.success(pettyCashService.getDetailWithUserInfo(id));
    }
    
    /**
     * 审批通过
     */
    @Operation(summary = "审批通过")
    @PostMapping("/approve/{taskId}")
    public Result<?> approve(@PathVariable String taskId, @RequestParam String opinion) {
        pettyCashService.approve(taskId, opinion);
        return Result.success("审批成功");
    }
    
    /**
     * 审批驳回
     */
    @Operation(summary = "审批驳回")
    @PostMapping("/reject/{taskId}")
    public Result<?> reject(@PathVariable String taskId, @RequestParam String opinion) {
        pettyCashService.reject(taskId, opinion);
        return Result.success("驳回成功");
    }
    
    /**
     * 获取审批历史记录
     */
    @Operation(summary = "获取审批历史记录")
    @GetMapping("/history/{id}")
    public Result<?> getApprovalHistory(@PathVariable Long id) {
        return Result.success(pettyCashService.getApprovalHistory(id));
    }
}
