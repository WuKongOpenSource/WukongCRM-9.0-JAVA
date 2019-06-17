
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for 72crm_crm_contacts_business
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_contacts_business`;
CREATE TABLE `72crm_crm_contacts_business` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `business_id` int(11) NOT NULL,
  `contacts_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='商机联系人关联表';
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('11', '联系人导出', 'excelexport', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('11', '联系人导入', 'excelimport', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('15', '产品导入', 'excelimport', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('15', '产品导出', 'excelexport', '3', '1', '1', NULL);
