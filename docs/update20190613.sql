SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for 72crm_crm_business_change
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_business_change`;
CREATE TABLE `72crm_crm_business_change` (
  `change_id` int(10) NOT NULL AUTO_INCREMENT,
  `business_id` int(10) NOT NULL COMMENT '商机ID',
  `status_id` int(10) NOT NULL COMMENT '阶段ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` int(10) NOT NULL COMMENT '创建人',
  PRIMARY KEY (`change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商机阶段变化表';
