package com.approval.mapper;

import com.approval.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据用户ID查询菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.visible = 1 " +
            "ORDER BY m.sort_order ASC, m.id ASC")
    List<SysMenu> selectMenusByUserId(Long userId);
    
    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("SELECT DISTINCT m.menu_code FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 " +
            "AND m.menu_code IS NOT NULL AND m.menu_code != ''")
    List<String> selectPermissionsByUserId(Long userId);
}
