package com.approval.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.approval.common.exception.BusinessException;
import com.approval.dto.LoginDTO;
import com.approval.entity.SysDept;
import com.approval.entity.SysMenu;
import com.approval.entity.SysRole;
import com.approval.entity.SysUser;
import com.approval.mapper.SysDeptMapper;
import com.approval.mapper.SysMenuMapper;
import com.approval.mapper.SysRoleMapper;
import com.approval.mapper.SysUserMapper;
import com.approval.service.AuthService;
import com.approval.vo.MenuVO;
import com.approval.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 登录
        StpUtil.login(user.getId());
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        
        return result;
    }
    
    @Override
    public UserInfoVO getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setDeptId(user.getDeptId());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        
        // 查询部门信息
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                userInfo.setDeptName(dept.getDeptName());
            }
        }
        
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        List<String> roleNames = roles.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toList());
        userInfo.setRoles(roleNames);
        
        // 查询用户权限
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);
        userInfo.setPermissions(permissions);
        
        return userInfo;
    }
    
    @Override
    public List<MenuVO> getUserMenus() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户菜单
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        
        // 转换为VO并构建树形结构
        return buildMenuTree(menus, 0L);
    }
    
    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuVO> menuTree = new ArrayList<>();
        
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                MenuVO menuVO = convertToMenuVO(menu);
                
                // 递归查找子菜单
                List<MenuVO> children = buildMenuTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    menuVO.setChildren(children);
                }
                
                menuTree.add(menuVO);
            }
        }
        
        return menuTree;
    }
    
    /**
     * 转换为 MenuVO
     */
    private MenuVO convertToMenuVO(SysMenu menu) {
        MenuVO menuVO = new MenuVO();
        menuVO.setId(menu.getId());
        menuVO.setParentId(menu.getParentId());
        menuVO.setMenuName(menu.getMenuName());
        menuVO.setMenuCode(menu.getMenuCode());
        menuVO.setMenuType(menu.getMenuType());
        menuVO.setPath(menu.getPath());
        menuVO.setComponent(menu.getComponent());
        menuVO.setIcon(menu.getIcon());
        menuVO.setSortOrder(menu.getSortOrder());
        return menuVO;
    }
    
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 加密新密码
        String encryptedPassword = BCrypt.hashpw(newPassword);
        
        // 更新密码
        user.setPassword(encryptedPassword);
        userMapper.updateById(user);
        
        // 密码修改成功后，强制退出当前登录状态
        StpUtil.logout(userId);
    }
}
