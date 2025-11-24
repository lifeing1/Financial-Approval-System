-- ==========================================
-- 修复 process_definition_info 表的唯一索引
-- ==========================================
-- 问题：原有的 process_key UNIQUE 约束不支持逻辑删除
-- 解决：将唯一约束改为联合唯一索引（包含 deleted 字段）
-- 日期：2025-11-24
-- ==========================================

USE approval_system;

-- 1. 删除原有的唯一约束
ALTER TABLE process_definition_info DROP INDEX process_key;

-- 2. 创建新的联合唯一索引（支持逻辑删除）
ALTER TABLE process_definition_info 
ADD UNIQUE INDEX uk_process_key_deleted (process_key, deleted);

