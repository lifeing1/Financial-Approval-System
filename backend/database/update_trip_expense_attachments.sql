-- 为出差费用明细表添加附件字段
ALTER TABLE biz_business_trip ADD COLUMN attachments VARCHAR(1000) COMMENT '附件URL（多个文件用逗号分隔）' AFTER remark;
