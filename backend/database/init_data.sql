USE approval_system;

-- 初始化部门数据
INSERT INTO sys_dept (id, parent_id, dept_name, sort_order, status) VALUES
(1, 0, '总公司', 1, 1),
(2, 1, '技术部', 1, 1),
(3, 1, '财务部', 2, 1),
(4, 1, '人事部', 3, 1),
(5, 1, '市场部', 4, 1);

-- 初始化角色数据
INSERT INTO sys_role (id, role_code, role_name, remark) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有系统所有权限'),
(2, 'GENERAL_MANAGER', '总经理', '公司最高审批权限'),
(3, 'FINANCE_MANAGER', '财务经理', '财务审批权限'),
(4, 'HR_MANAGER', '人事经理', '人事审批权限'),
(5, 'DEPT_MANAGER', '部门经理', '部门管理权限及审批权限'),
(6, 'EMPLOYEE', '普通员工', '基础使用权限');

-- 初始化用户数据（密码均为：123456，使用 BCrypt 加密）
INSERT INTO sys_user (id, username, password, real_name, dept_id, phone, email, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, '13800000000', 'admin@company.com', 1),
(2, 'ceo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王总', 1, '13800000001', 'ceo@company.com', 1),
(3, 'finance_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '财务经理李芳', 3, '13800000002', 'finance@company.com', 1),
(4, 'hr_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '人事经理张敏', 4, '13800000003', 'hr@company.com', 1),
(5, 'tech_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '技术部经理刘强', 2, '13800000004', 'tech@company.com', 1),
(6, 'market_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '市场部经理赵敏', 5, '13800000005', 'market@company.com', 1),
(7, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '员工张三', 2, '13800000006', 'zhangsan@company.com', 1),
(8, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '员工李四', 2, '13800000007', 'lisi@company.com', 1),
(9, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '员工王五', 5, '13800000008', 'wangwu@company.com', 1);

-- 更新部门负责人
UPDATE sys_dept SET leader_id = 5 WHERE id = 2;  -- 技术部经理
UPDATE sys_dept SET leader_id = 3 WHERE id = 3;  -- 财务经理
UPDATE sys_dept SET leader_id = 4 WHERE id = 4;  -- 人事经理
UPDATE sys_dept SET leader_id = 6 WHERE id = 5;  -- 市场部经理

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin 为超级管理员
(2, 2),  -- ceo 为总经理
(3, 3),  -- finance_mgr 为财务经理
(4, 4),  -- hr_mgr 为人事经理
(5, 5),  -- tech_mgr 为部门经理（技术部）
(6, 5),  -- market_mgr 为部门经理（市场部）
(7, 6),  -- zhangsan 为普通员工
(8, 6),  -- lisi 为普通员工
(9, 6);  -- wangwu 为普通员工

-- 初始化菜单数据
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, visible) VALUES
-- 一级菜单
(1, 0, '工作台', 'dashboard', 1, '/dashboard', 'Dashboard', 'home', 1, 1),
(2, 0, '出差管理', 'business_trip', 1, '/business-trip', NULL, 'airplane', 2, 1),
(3, 0, '备用金管理', 'petty_cash', 1, '/petty-cash', NULL, 'cash', 3, 1),
(4, 0, '流程管理', 'workflow', 1, '/workflow', NULL, 'git-network', 4, 1),
(5, 0, '数据统计', 'statistics', 1, '/statistics', NULL, 'bar-chart', 5, 1),
(6, 0, '个人中心', 'profile', 1, '/profile', 'Profile', 'person', 6, 1),

-- 出差管理子菜单
(21, 2, '出差申请', 'business_trip_apply', 1, '/business-trip/apply', 'BusinessTripApply', NULL, 1, 1),
(22, 2, '待办审批', 'business_trip_todo', 1, '/business-trip/todo', 'BusinessTripTodo', NULL, 2, 1),
(23, 2, '已办审批', 'business_trip_done', 1, '/business-trip/done', 'BusinessTripDone', NULL, 3, 1),
(24, 2, '我的申请', 'business_trip_my', 1, '/business-trip/my', 'BusinessTripMy', NULL, 4, 1),

-- 备用金管理子菜单
(31, 3, '备用金申请', 'petty_cash_apply', 1, '/petty-cash/apply', 'PettyCashApply', NULL, 1, 1),
(32, 3, '待办审批', 'petty_cash_todo', 1, '/petty-cash/todo', 'PettyCashTodo', NULL, 2, 1),
(33, 3, '已办审批', 'petty_cash_done', 1, '/petty-cash/done', 'PettyCashDone', NULL, 3, 1),
(34, 3, '我的申请', 'petty_cash_my', 1, '/petty-cash/my', 'PettyCashMy', NULL, 4, 1),

-- 流程管理子菜单
(41, 4, '流程列表', 'workflow_list', 1, '/workflow/list', 'WorkflowList', NULL, 1, 1),

-- 数据统计子菜单
(51, 5, '出差统计', 'statistics_business_trip', 1, '/statistics/business-trip', 'BusinessTripStats', NULL, 1, 1),
(52, 5, '备用金统计', 'statistics_petty_cash', 1, '/statistics/petty-cash', 'PettyCashStats', NULL, 2, 1),
(53, 5, '审批效率', 'statistics_efficiency', 1, '/statistics/efficiency', 'EfficiencyStats', NULL, 3, 1);

-- 初始化角色菜单关联（超级管理员拥有所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 总经理菜单（拥有所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu;

-- 财务经理菜单（除流程管理外的所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE id NOT IN (4, 41);

-- 人事经理菜单（除流程管理外的所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, id FROM sys_menu WHERE id NOT IN (4, 41);

-- 部门经理菜单（除流程管理外的所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 5, id FROM sys_menu WHERE id NOT IN (4, 41);

-- 普通员工菜单（除流程管理和数据统计外的基础菜单）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 6, id FROM sys_menu WHERE id NOT IN (4, 41, 5, 51, 52, 53);
