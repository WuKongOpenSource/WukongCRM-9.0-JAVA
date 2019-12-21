SET FOREIGN_KEY_CHECKS=0;

ALTER TABLE `72crm_admin_field_sort` ADD COLUMN `type` int(2) NULL DEFAULT NULL COMMENT '字段类型 1 单行文本 2 多行文本 3 单选 4日期 5 数字 6 小数 7 手机  8 文件 9 多选 10 人员 11 附件 12 部门 13 日期时间 14 邮箱 15客户 16 商机 17 联系人 18 地图 19 产品类型 20 合同 21 回款计划' AFTER `name`;

CREATE TABLE `72crm_admin_message`  (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容',
  `label` int(2) NULL DEFAULT NULL COMMENT '消息大类 1 任务 2 日志 3 oa审批 4公告 5 日程 6 crm消息',
  `type` int(2) NULL DEFAULT NULL COMMENT '消息类型 详见AdminMessageEnum',
  `type_id` int(11) NULL DEFAULT NULL COMMENT '关联ID',
  `create_user` bigint(20) NOT NULL COMMENT '消息创建者 0为系统',
  `recipient_user` bigint(20) NOT NULL COMMENT '接收人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `is_read` int(1) NULL DEFAULT 0 COMMENT '是否已读 0 未读 1 已读',
  `read_time` datetime(0) NULL DEFAULT NULL COMMENT '已读时间',
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统消息表' ROW_FORMAT = Dynamic;

ALTER TABLE `72crm_crm_contract_product` MODIFY COLUMN `discount` decimal(10, 2) NOT NULL COMMENT '折扣' AFTER `num`;

ALTER TABLE `72crm_crm_customer` ADD COLUMN `last_content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `batch_id`;

ALTER TABLE `72crm_crm_leads` ADD COLUMN `last_content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `batch_id`;

ALTER TABLE `72crm_crm_product` MODIFY COLUMN `status` int(1) NULL DEFAULT 0 COMMENT '状态 1 上架 0 下架 3 删除' AFTER `price`;

SET FOREIGN_KEY_CHECKS=1;