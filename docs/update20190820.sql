ALTER TABLE `72crm_admin_field`
ADD COLUMN `relevant`  int(11) NULL COMMENT '只有线索需要，装换客户的自定义字段ID' AFTER `field_type`;

ALTER TABLE `72crm_crm_contract`
ADD COLUMN `total_price`  decimal(10,2) NULL COMMENT '产品总金额' AFTER `discount_rate`;

ALTER TABLE `72crm_crm_business`
ADD COLUMN `total_price`  decimal(10,2) NULL COMMENT '产品总金额' AFTER `discount_rate`;

INSERT INTO `72crm_admin_menu` VALUES ('146', '2', '办公分析', 'oa', '1', '10', '1', null);
INSERT INTO `72crm_admin_menu` VALUES ('147', '146', '查看', 'read', '3', '1', '1', null);