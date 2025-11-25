package com.approval.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.entity.BusinessTrip;
import com.approval.entity.PettyCash;
import com.approval.entity.SysDept;
import com.approval.entity.SysRole;
import com.approval.entity.SysUser;
import com.approval.mapper.BusinessTripMapper;
import com.approval.mapper.PettyCashMapper;
import com.approval.mapper.SysDeptMapper;
import com.approval.mapper.SysRoleMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.service.StatisticsService;
import com.approval.vo.CashStatisticsVO;
import com.approval.vo.TripStatisticsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private final BusinessTripMapper businessTripMapper;
    private final PettyCashMapper pettyCashMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
    @Override
    public TripStatisticsVO getTripStatistics(String startDate, String endDate) {
        // 获取当前登录用户
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(userId);
        
        // 判断用户角色
        boolean isDeptLeader = isDeptLeader(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<BusinessTrip> wrapper = new LambdaQueryWrapper<>();
        // 统计已提交的申请（审批中、已通过、已驳回），排除草稿
        wrapper.in(BusinessTrip::getStatus, 1, 2, 3);
        
        // 部门负责人只能看自己部门的数据
        if (isDeptLeader && currentUser.getDeptId() != null) {
            wrapper.eq(BusinessTrip::getDeptId, currentUser.getDeptId());
        }
        
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(BusinessTrip::getApplyDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(BusinessTrip::getApplyDate, LocalDate.parse(endDate));
        }
        
        List<BusinessTrip> tripList = businessTripMapper.selectList(wrapper);
        
        TripStatisticsVO vo = new TripStatisticsVO();
        
        // 按部门统计
        vo.setDeptStats(calculateDeptStats(tripList));
        
        // 按月份统计
        vo.setMonthStats(calculateMonthStats(tripList));
        
        // 按申请人统计
        vo.setUserStats(calculateUserStats(tripList));
        
        return vo;
    }
    
    /**
     * 按部门统计出差数据
     */
    private List<TripStatisticsVO.DeptStatItem> calculateDeptStats(List<BusinessTrip> tripList) {
        // 按部门ID分组
        Map<Long, List<BusinessTrip>> deptGroupMap = tripList.stream()
                .collect(Collectors.groupingBy(BusinessTrip::getDeptId));
        
        // 查询所有部门信息
        List<SysDept> allDepts = sysDeptMapper.selectList(null);
        Map<Long, String> deptNameMap = allDepts.stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        
        List<TripStatisticsVO.DeptStatItem> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<BusinessTrip>> entry : deptGroupMap.entrySet()) {
            TripStatisticsVO.DeptStatItem item = new TripStatisticsVO.DeptStatItem();
            item.setDeptName(deptNameMap.getOrDefault(entry.getKey(), "未知部门"));
            item.setCount((long) entry.getValue().size());
            
            BigDecimal totalAmount = entry.getValue().stream()
                    .map(trip -> trip.getTotalAmount() != null ? trip.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setTotalAmount(totalAmount);
            
            result.add(item);
        }
        
        // 按金额降序排序
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        
        return result;
    }
    
    /**
     * 按月份统计出差数据
     */
    private List<TripStatisticsVO.MonthStatItem> calculateMonthStats(List<BusinessTrip> tripList) {
        // 按月份分组
        Map<String, List<BusinessTrip>> monthGroupMap = tripList.stream()
                .filter(trip -> trip.getApplyDate() != null)
                .collect(Collectors.groupingBy(trip -> trip.getApplyDate().format(MONTH_FORMATTER)));
        
        List<TripStatisticsVO.MonthStatItem> result = new ArrayList<>();
        
        for (Map.Entry<String, List<BusinessTrip>> entry : monthGroupMap.entrySet()) {
            TripStatisticsVO.MonthStatItem item = new TripStatisticsVO.MonthStatItem();
            item.setMonth(entry.getKey());
            item.setCount((long) entry.getValue().size());
            
            BigDecimal totalAmount = entry.getValue().stream()
                    .map(trip -> trip.getTotalAmount() != null ? trip.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setTotalAmount(totalAmount);
            
            result.add(item);
        }
        
        // 按月份排序
        result.sort(Comparator.comparing(TripStatisticsVO.MonthStatItem::getMonth));
        
        return result;
    }
    
    /**
     * 按申请人统计出差数据
     */
    private List<TripStatisticsVO.UserStatItem> calculateUserStats(List<BusinessTrip> tripList) {
        // 按用户ID分组
        Map<Long, List<BusinessTrip>> userGroupMap = tripList.stream()
                .collect(Collectors.groupingBy(BusinessTrip::getUserId));
        
        // 查询所有用户信息
        List<SysUser> allUsers = sysUserMapper.selectList(null);
        Map<Long, String> userNameMap = allUsers.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        
        List<TripStatisticsVO.UserStatItem> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<BusinessTrip>> entry : userGroupMap.entrySet()) {
            TripStatisticsVO.UserStatItem item = new TripStatisticsVO.UserStatItem();
            item.setUserName(userNameMap.getOrDefault(entry.getKey(), "未知用户"));
            item.setCount((long) entry.getValue().size());
            
            BigDecimal totalAmount = entry.getValue().stream()
                    .map(trip -> trip.getTotalAmount() != null ? trip.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setTotalAmount(totalAmount);
            
            result.add(item);
        }
        
        // 按次数降序排序，取前10名
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        
        return result.stream().limit(10).collect(Collectors.toList());
    }
    
    @Override
    public CashStatisticsVO getCashStatistics(String startDate, String endDate) {
        // 获取当前登录用户
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(userId);
        
        // 判断用户角色
        boolean isDeptLeader = isDeptLeader(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<PettyCash> wrapper = new LambdaQueryWrapper<>();
        // 统计已提交的申请（审批中、已通过、已驳回），排除草稿
        wrapper.in(PettyCash::getStatus, 1, 2, 3);
        
        // 部门负责人只能看自己部门的数据
        if (isDeptLeader && currentUser.getDeptId() != null) {
            wrapper.eq(PettyCash::getDeptId, currentUser.getDeptId());
        }
        
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(PettyCash::getApplyDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(PettyCash::getApplyDate, LocalDate.parse(endDate));
        }
        
        List<PettyCash> cashList = pettyCashMapper.selectList(wrapper);
        
        CashStatisticsVO vo = new CashStatisticsVO();
        
        // 按部门统计
        vo.setDeptStats(calculateCashDeptStats(cashList));
        
        // 按月份统计
        vo.setMonthStats(calculateCashMonthStats(cashList));
        
        return vo;
    }
    
    /**
     * 按部门统计备用金数据
     */
    private List<CashStatisticsVO.DeptStatItem> calculateCashDeptStats(List<PettyCash> cashList) {
        // 按部门ID分组
        Map<Long, List<PettyCash>> deptGroupMap = cashList.stream()
                .collect(Collectors.groupingBy(PettyCash::getDeptId));
        
        // 查询所有部门信息
        List<SysDept> allDepts = sysDeptMapper.selectList(null);
        Map<Long, String> deptNameMap = allDepts.stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        
        List<CashStatisticsVO.DeptStatItem> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<PettyCash>> entry : deptGroupMap.entrySet()) {
            CashStatisticsVO.DeptStatItem item = new CashStatisticsVO.DeptStatItem();
            item.setDeptName(deptNameMap.getOrDefault(entry.getKey(), "未知部门"));
            
            List<PettyCash> cashListByDept = entry.getValue();
            
            // 总申请数和总申请金额
            item.setApplyCount((long) cashListByDept.size());
            BigDecimal applyAmount = cashListByDept.stream()
                    .map(cash -> cash.getAmount() != null ? cash.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setApplyAmount(applyAmount);
            
            // 已通过的数量和金额
            List<PettyCash> approvedList = cashListByDept.stream()
                    .filter(cash -> cash.getStatus() == 2)
                    .collect(Collectors.toList());
            item.setApprovedCount((long) approvedList.size());
            BigDecimal approvedAmount = approvedList.stream()
                    .map(cash -> cash.getAmount() != null ? cash.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setApprovedAmount(approvedAmount);
            
            result.add(item);
        }
        
        // 按申请金额降序排序
        result.sort((a, b) -> b.getApplyAmount().compareTo(a.getApplyAmount()));
        
        return result;
    }
    
    /**
     * 按月份统计备用金数据
     */
    private List<CashStatisticsVO.MonthStatItem> calculateCashMonthStats(List<PettyCash> cashList) {
        // 按月份分组
        Map<String, List<PettyCash>> monthGroupMap = cashList.stream()
                .filter(cash -> cash.getApplyDate() != null)
                .collect(Collectors.groupingBy(cash -> cash.getApplyDate().format(MONTH_FORMATTER)));
        
        List<CashStatisticsVO.MonthStatItem> result = new ArrayList<>();
        
        for (Map.Entry<String, List<PettyCash>> entry : monthGroupMap.entrySet()) {
            CashStatisticsVO.MonthStatItem item = new CashStatisticsVO.MonthStatItem();
            item.setMonth(entry.getKey());
            
            List<PettyCash> cashListByMonth = entry.getValue();
            
            // 总申请数和总申请金额
            item.setApplyCount((long) cashListByMonth.size());
            BigDecimal applyAmount = cashListByMonth.stream()
                    .map(cash -> cash.getAmount() != null ? cash.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setApplyAmount(applyAmount);
            
            // 已通过的数量和金额
            List<PettyCash> approvedList = cashListByMonth.stream()
                    .filter(cash -> cash.getStatus() == 2)
                    .collect(Collectors.toList());
            item.setApprovedCount((long) approvedList.size());
            BigDecimal approvedAmount = approvedList.stream()
                    .map(cash -> cash.getAmount() != null ? cash.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setApprovedAmount(approvedAmount);
            
            result.add(item);
        }
        
        // 按月份排序
        result.sort(Comparator.comparing(CashStatisticsVO.MonthStatItem::getMonth));
        
        return result;
    }
    
    /**
     * 判断用户是否为部门负责人
     */
    private boolean isDeptLeader(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .anyMatch(role -> "DEPT_LEADER".equals(role.getRoleCode()));
    }
}
