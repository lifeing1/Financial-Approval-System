-- 创建数据库
CREATE DATABASE IF NOT EXISTS approval_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE approval_system;

-- 1. 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    dept_id BIGINT COMMENT '部门ID',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 菜单表
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) COMMENT '菜单编码',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型 1-菜单 2-按钮',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见 0-隐藏 1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 5. 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 6. 部门表
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    leader_id BIGINT COMMENT '负责人ID',
    phone VARCHAR(20) COMMENT '联系电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 7. 出差申请表
CREATE TABLE biz_business_trip (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    apply_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请编号',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    apply_date DATE NOT NULL COMMENT '申请日期',
    reason VARCHAR(500) NOT NULL COMMENT '出差事由',
    start_place VARCHAR(100) NOT NULL COMMENT '出发地',
    destination VARCHAR(100) NOT NULL COMMENT '目的地',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    transport_modes VARCHAR(200) COMMENT '交通方式（多选，逗号分隔）',
    total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '预计总费用',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 0 COMMENT '状态 0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_apply_no (apply_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_process_instance_id (process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出差申请表';

-- 8. 出差费用明细表
CREATE TABLE biz_trip_expense (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    trip_id BIGINT NOT NULL COMMENT '出差申请ID',
    expense_type VARCHAR(50) NOT NULL COMMENT '费用类型',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_trip_id (trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出差费用明细表';

-- 9. 备用金申请表
CREATE TABLE biz_petty_cash (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    apply_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请编号',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    apply_date DATE NOT NULL COMMENT '申请日期',
    reason VARCHAR(500) NOT NULL COMMENT '申请事由',
    amount DECIMAL(10,2) NOT NULL COMMENT '申请金额',
    use_period VARCHAR(100) COMMENT '使用期限',
    repayment_plan VARCHAR(500) COMMENT '还款计划',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 0 COMMENT '状态 0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_apply_no (apply_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_process_instance_id (process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备用金申请表';

-- 10. 附件表
CREATE TABLE biz_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小',
    file_type VARCHAR(50) COMMENT '文件类型',
    upload_user_id BIGINT COMMENT '上传人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_business (business_id, business_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- 11. 审批意见表
CREATE TABLE biz_approval_opinion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    task_id VARCHAR(64) COMMENT '任务ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    opinion TEXT COMMENT '审批意见',
    action VARCHAR(20) COMMENT '操作 approve-同意 reject-驳回 transfer-转交',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_business (business_id, business_type),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';

-- 12. 流程定义信息扩展表
CREATE TABLE process_definition_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程定义KEY',
    process_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    category VARCHAR(50) COMMENT '流程分类',
    description VARCHAR(500) COMMENT '流程描述',
    deployment_id VARCHAR(64) COMMENT '最新部署ID',
    process_definition_id VARCHAR(64) COMMENT '最新流程定义ID',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态 0-暂停 1-激活',
    process_config TEXT COMMENT '流程配置JSON',
    create_user BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE INDEX uk_process_key_deleted (process_key, deleted) COMMENT '流程KEY唯一索引（支持逻辑删除）',
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义信息扩展表';

-- 13. 流程节点配置表
CREATE TABLE process_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_key VARCHAR(100) NOT NULL COMMENT '流程定义KEY',
    node_key VARCHAR(100) NOT NULL COMMENT '节点KEY',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(50) DEFAULT 'user_task' COMMENT '节点类型',
    assignee_type VARCHAR(50) COMMENT '审批人类型',
    assignee_config TEXT COMMENT '审批人配置JSON',
    allow_reject TINYINT DEFAULT 1 COMMENT '是否允许驳回',
    require_attachment TINYINT DEFAULT 0 COMMENT '是否需要附件',
    timeout_hours INT DEFAULT 0 COMMENT '超时小时数',
    timeout_reminder TEXT COMMENT '超时提醒配置JSON',
    node_order INT DEFAULT 0 COMMENT '节点顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_process_key (process_key),
    INDEX idx_node_key (node_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点配置表';
