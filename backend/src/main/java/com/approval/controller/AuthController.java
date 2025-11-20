package com.approval.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.result.Result;
import com.approval.dto.ChangePasswordDTO;
import com.approval.dto.LoginDTO;
import com.approval.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }
    
    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success("登出成功");
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<?> getUserInfo() {
        return Result.success(authService.getUserInfo());
    }
    
    /**
     * 获取用户菜单
     */
    @Operation(summary = "获取用户菜单")
    @GetMapping("/menus")
    public Result<?> getUserMenus() {
        return Result.success(authService.getUserMenus());
    }
    
    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功");
    }
}
