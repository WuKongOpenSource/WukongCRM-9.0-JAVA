-- ----------------------------
-- Table structure for 72crm_crm_customer_setting
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_customer_setting`;
CREATE TABLE `72crm_crm_customer_setting` (
  `setting_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_name` varchar(255) DEFAULT NULL COMMENT '规则名称',
  `customer_num` int(11) DEFAULT NULL COMMENT '可拥有客户数量',
  `customer_deal` int(1) DEFAULT '0' COMMENT '成交客户是否占用数量 0 不占用 1 占用',
  `type` int(1) DEFAULT NULL COMMENT '类型 1 拥有客户数限制 2 锁定客户数限制',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`setting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COMMENT='员工拥有以及锁定客户数限制';

-- ----------------------------
-- Table structure for 72crm_crm_customer_settinguser
-- ----------------------------
DROP TABLE IF EXISTS `72crm_crm_customer_settinguser`;
CREATE TABLE `72crm_crm_customer_settinguser` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `setting_id` int(11) NOT NULL COMMENT '客户规则限制ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `dept_id` int(11) DEFAULT NULL COMMENT '部门ID',
  `type` int(1) DEFAULT NULL COMMENT '1 员工 2 部门',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COMMENT='员工拥有以及锁定客户员工关联表';

ALTER TABLE `72crm_crm_business_product`
MODIFY COLUMN `discount`  decimal(10,2) NOT NULL COMMENT '折扣' AFTER `num`;

ALTER TABLE `72crm_admin_user`
MODIFY COLUMN `parent_id`  bigint(20) NULL DEFAULT NULL COMMENT '直属上级ID' AFTER `status`;

UPDATE `72crm_admin_role` SET `remark` = 'project',`role_type` = 8 WHERE `role_name` = '项目管理员';
UPDATE `72crm_admin_role` SET `remark` = 'admin' WHERE `role_name` = '超级管理员' AND `role_id` = 1;
alter table 72crm_admin_role modify column role_type int(1) comment '0、自定义角色1、管理角色 2、客户管理角色 3、人事角色 4、财务角色 5、项目角色 6、项目自定义角色 7、办公角色 8、项目管理角色';
UPDATE `72crm_admin_role` SET `role_type` = 2 WHERE `role_type` IN (0,3,4);
UPDATE `72crm_admin_role` SET `role_type` = 7 WHERE `role_id` = 7;
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 100 AND `realm` = 'product';
UPDATE `72crm_admin_menu` SET `menu_id`='101', `parent_id`='2', `menu_name`='员工业绩分析', `realm`='contract', `menu_type`='1', `sort`='0', `status`='1', `remarks`=NULL WHERE (`menu_id`='101');
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 105 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 111 AND `realm` = 'customer';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 112 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 113 AND `realm` = 'funnel';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 114 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 115 AND `realm` = 'receivables';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 116 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 119 AND `realm` = 'performance';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 120 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 121 AND `realm` = 'employe';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 122 AND `realm` = 'read';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 91 AND `realm` = 'crm';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 92 AND `realm` = 'examineFlow';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 93 AND `realm` = 'oa';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 94 AND `realm` = 'permission';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 95 AND `realm` = 'system';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 96 AND `realm` = 'user';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 37 AND `realm` = 'pool';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 38 AND `realm` = 'distribute';
DELETE FROM `72crm_admin_menu` WHERE `menu_id` = 39 AND `realm` = 'receive';
UPDATE `72crm_admin_menu` SET `menu_id`='1', `parent_id`='0', `menu_name`='客户管理', `realm`='crm', `menu_type`='0', `sort`='0', `status`='1', `remarks`=NULL WHERE (`menu_id`='1');
DELETE FROM `72crm_admin_role_menu` WHERE `menu_id` NOT IN (SELECT `menu_id` FROM `72crm_admin_menu`);
DELETE FROM `72crm_admin_role_menu` WHERE `menu_id` IN (2,3,146,147);
UPDATE `72crm_admin_menu` SET `menu_id`='1', `parent_id`='0', `menu_name`='全部', `realm`='crm', `menu_type`='0', `sort`='0', `status`='1', `remarks`=NULL WHERE (`menu_id`='1');
UPDATE `72crm_admin_menu` SET `menu_id`='2', `parent_id`='0', `menu_name`='全部', `realm`='bi', `menu_type`='0', `sort`='0', `status`='1', `remarks`=NULL WHERE (`menu_id`='2');
UPDATE `72crm_admin_menu` SET `menu_id`='3', `parent_id`='0', `menu_name`='全部', `realm`='manage', `menu_type`='0', `sort`='0', `status`='1', `remarks`=NULL WHERE (`menu_id`='3');
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('148', '0', '全部', 'oa', '0', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('149', '0', '全部', 'project', '0', '0', '1', '项目管理角色权限');
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('150', '148', '通讯录', 'book', '1', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('151', '150', '查看', 'read', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('152', '149', '项目管理', 'projectManage', '1', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('153', '152', '新建项目', 'save', '3', '2', '1', 'projectSave');
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('160', '3', '企业首页', 'system', '1', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('161', '160', '查看', 'read', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('162', '160', '编辑', 'update', '3', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('163', '3', '应用管理', 'configSet', '1', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('164', '163', '查看', 'read', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('165', '163', '停用/启用', 'update', '3', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('166', '3', '员工与部门管理', 'users', '1', '3', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('167', '166', '部门/员工查看', 'read', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('168', '166', '员工新建', 'userSave', '3', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('169', '166', '员工禁用/激活', 'userEnables', '3', '3', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('170', '166', '员工操作', 'userUpdate', '3', '4', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('171', '166', '部门新建', 'deptSave', '3', '5', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('172', '166', '部门编辑', 'deptUpdate', '3', '6', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('173', '166', '部门删除', 'deptDelete', '3', '7', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('174', '3', '角色权限管理', 'permission', '1', '4', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('175', '174', '角色权限设置', 'update', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('176', '3', '工作台设置', 'oa', '1', '5', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('177', '176', '办公审批管理', 'examine', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('178', '3', '审批流程管理', 'examineFlow', '1', '6', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('179', '178', '审批流程管理', 'update', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('180', '3', '客户管理设置', 'crm', '1', '7', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('181', '180', '自定义字段设置', 'field', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('182', '180', '客户公海规则', 'pool', '3', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('183', '180', '业务参数设置', 'setting', '3', '3', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('184', '180', '业绩目标设置', 'achievement', '3', '4', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('185', '3', '项目管理设置', 'work', '1', '8', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('186', '185', '项目管理', 'update', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('187', '148', '公告', 'announcement', '1', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('188', '187', '新建', 'save', '3', '1', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('189', '187', '编辑', 'update', '3', '2', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('190', '187', '删除', 'delete', '3', '3', '1', NULL);
INSERT INTO `72crm_admin_menu` (`menu_id`, `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ('191', '10', '设置成交状态', 'dealStatus', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '166');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '167');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '168');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '169');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '170');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '171');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '172');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('3', '173');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('4', '178');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('4', '179');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('5', '176');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('5', '177');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('6', '180');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('6', '181');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('6', '182');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('6', '183');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('6', '184');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('7', '187');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('7', '188');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('7', '189');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('7', '190');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '3');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '160');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '161');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '162');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '163');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '164');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '165');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '166');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '167');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '168');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '169');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '170');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '171');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '172');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '173');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '174');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '175');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '176');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '177');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '178');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '179');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '180');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '181');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '182');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '183');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '184');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '185');
INSERT INTO `72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES ('2', '186');

INSERT INTO `72crm_admin_config` VALUES (null, '1', 'putInPoolRemindDays', '2', null);
INSERT INTO `72crm_admin_config` VALUES (null, '1', 'oa', '1', '办公管理');
INSERT INTO `72crm_admin_config` VALUES (null, '1', 'crm', '1', '客户管理');
INSERT INTO `72crm_admin_config` VALUES (null, '1', 'project', '1', '项目管理');
INSERT INTO `72crm_admin_config` VALUES (null, '0', 'hrm', '2', '人力资源管理');
INSERT INTO `72crm_admin_config` VALUES (null, '0', 'jxc', '2', '进销存管理');

DELETE FROM 72crm_admin_field WHERE field_name = 'deal_status' AND name = '成交状态' AND label = 2;

UPDATE 72crm_admin_field SET field_name = 'end_time' WHERE field_name = 'end_tme';