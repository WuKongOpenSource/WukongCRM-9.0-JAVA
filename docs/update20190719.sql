
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for 72crm_work_user
-- ----------------------------
DROP TABLE IF EXISTS `72crm_work_user`;
CREATE TABLE `72crm_work_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `work_id` int(11) NOT NULL COMMENT '项目ID',
  `user_id` int(11) NOT NULL COMMENT '成员ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='项目成员表';

ALTER TABLE 72crm_work_task_lable RENAME TO 72crm_work_task_label;

ALTER TABLE `72crm_work_task_label`
CHANGE COLUMN `lable_id` `label_id`  int(11) NOT NULL AUTO_INCREMENT FIRST ;

ALTER TABLE `72crm_admin_role`
ADD COLUMN `is_hidden`  int(1) NULL DEFAULT 1 COMMENT '0 隐藏 1 不隐藏' AFTER `data_type`;

DROP TABLE IF EXISTS `72crm_work_relation`;

ALTER TABLE `72crm_task`
CHANGE COLUMN `lable_id` `label_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标签 ,号拼接' AFTER `class_id`;

ALTER TABLE `72crm_task`
ADD COLUMN `is_archive`  int(1) NULL DEFAULT 0 COMMENT '是否归档 0 否 1 是' AFTER `batch_id`;

ALTER TABLE `72crm_admin_role`
ADD COLUMN `label`  int(2) NULL COMMENT '1 系统项目管理员角色 2 项目管理角色 3 项目编辑角色 4 项目只读角色' AFTER `is_hidden`;

DELETE FROM `72crm_admin_role` WHERE (`role_type`='5')

INSERT INTO `72crm_admin_role` (`role_name`, `role_type`, `is_hidden`,  `label`) VALUES ('项目管理员', '1', '1','1');
INSERT INTO `72crm_admin_role` (`role_name`, `role_type`, `is_hidden`, `label`) VALUES ('管理', '5', '0','2');
INSERT INTO `72crm_admin_role` (`role_name`, `role_type`, `is_hidden`, `label`) VALUES ('编辑', '5', '1','3');
set @rid =  last_insert_id();
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( '0', '项目管理', 'work', '0', '0', '1', NULL);
set @workId =  last_insert_id();
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @workId, '任务', 'task', '1', '0', '1', NULL);
set @taskId =  last_insert_id();
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @taskId, '创建', 'save', '3', '0', '1', NULL);
INSERT INTO`72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES (@rid, last_insert_id());
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @workId, '任务列表', 'taskClass', '1', '0', '1', NULL);
set @classId =  last_insert_id();
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @classId, '新建任务列表', 'save', '3', '0', '1', NULL);
INSERT INTO`72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES (@rid, last_insert_id());
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @classId, '编辑任务列表', 'update', '3', '0', '1', NULL);
INSERT INTO`72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES (@rid, last_insert_id());
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @classId, '删除任务列表', 'delete', '3', '0', '1', NULL);
INSERT INTO`72crm_admin_role_menu` (`role_id`, `menu_id`) VALUES (@rid, last_insert_id());
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @workId, '项目', 'work', '1', '0', '1', NULL);
set @wId =  last_insert_id();
INSERT INTO `72crm_admin_menu` ( `parent_id`, `menu_name`, `realm`, `menu_type`, `sort`, `status`, `remarks`) VALUES ( @wId, '项目设置', 'update', '3', '0', '1', NULL);
INSERT INTO `72crm_admin_role` (`role_name`, `role_type`, `is_hidden`, `label`) VALUES ('只读', '5', '1','4');

ALTER TABLE `72crm_task`
MODIFY COLUMN `order_num`  int(4) NULL DEFAULT 999 COMMENT '排序ID' AFTER `is_open`,
MODIFY COLUMN `top_order_num`  int(4) NULL DEFAULT 999 COMMENT '我的任务排序ID' AFTER `order_num`;