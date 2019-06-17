
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for 72crm_crm_owner_record
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_owner_record`;
CREATE TABLE `72crm_crm_owner_record` (
  `record_id` int(11) NOT NULL AUTO_INCREMENT,
  `type_id` int(11) NOT NULL COMMENT '对象id',
  `type` int(4) NOT NULL COMMENT '对象类型',
  `pre_owner_user_id` int(20) DEFAULT NULL COMMENT '上一负责人',
  `post_owner_user_id` int(20) DEFAULT NULL COMMENT '接手负责人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='负责人变更记录表';


SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for 72crm_crm_customer_stats
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_customer_stats`;
CREATE TABLE `72crm_crm_customer_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(20) NOT NULL COMMENT '用户id',
  `customer_num` int(10) NOT NULL DEFAULT '0' COMMENT '客户总数',
  `create_time` datetime NOT NULL COMMENT '统计时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=190 DEFAULT CHARSET=utf8 COMMENT='每日客户统计';

INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '员工客户分析', 'customer', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('111', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '销售漏斗', 'funnel', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('113', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '回款统计', 'receivables', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('115', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '产品分析', 'product', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('117', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '业绩目标完成情况', 'performance', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('119', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '员工业绩分析', 'employe', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('121', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '客户画像分析', 'portrait', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('123', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('2', '排行榜', 'ranking', '1', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('125', '查看', 'read', '3', '0', '1', NULL);
