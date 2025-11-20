package com.approval.vo;

import lombok.Data;
import java.util.List;

/**
 * 菜单 VO
 */
@Data
public class MenuVO {
    
    private Long id;
    
    private Long parentId;
    
    private String menuName;
    
    private String menuCode;
    
    private Integer menuType;
    
    private String path;
    
    private String component;
    
    private String icon;
    
    private Integer sortOrder;
    
    private List<MenuVO> children;
}
