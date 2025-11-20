package com.approval.service;

import com.approval.dto.LoginDTO;
import com.approval.vo.UserInfoVO;
import com.approval.vo.MenuVO;
import java.util.List;
import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(LoginDTO loginDTO);
    
    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo();
    
    /**
     * 获取用户菜单
     */
    List<MenuVO> getUserMenus();
    
    /**
     * 修改密码
     */
    void changePassword(String oldPassword, String newPassword);
}
