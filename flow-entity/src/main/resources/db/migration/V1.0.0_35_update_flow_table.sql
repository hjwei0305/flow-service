CREATE TABLE `flow_disagree_reason` (
`id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`flow_type_id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联流程类型ID' ,
`flow_type_name`  varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程类型名称' ,
`code`  varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原因code' ,
`name`  varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原因简称' ,
`depict`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原因描述' ,
`rank`  int(11) NULL DEFAULT NULL COMMENT '排序' ,
`status`  tinyint(1) NULL DEFAULT 0 COMMENT '启用状态' ,
`tenant_code`  varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户代码' ,
`creator_id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人ID' ,
`creator_account`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者账号' ,
`creator_name`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者名称' ,
`created_date`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
`last_editor_id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后更新者' ,
`last_editor_account`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后更新者账号' ,
`last_editor_name`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后更新者名称' ,
`last_edited_date`  datetime NULL DEFAULT NULL COMMENT '最后更新时间' ,
PRIMARY KEY (`id`),
UNIQUE INDEX `idx_reason_code_tenant` (`code`, `tenant_code`) USING BTREE COMMENT '每个租户下的原因代码不能重复',
INDEX `idx_reason_flow_type_id` (`flow_type_id`) USING BTREE 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci
ROW_FORMAT=COMPACT
;


ALTER TABLE `flow_history`
ADD COLUMN `disagree_reason_id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '不同意原因ID' AFTER `act_history_id`,
ADD COLUMN `disagree_reason_code`  varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '不同意原因code' AFTER `disagree_reason_id`,
ADD COLUMN `disagree_reason_name`  varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '不同意原因简称' AFTER `disagree_reason_code`;


/* 优化 */
ALTER TABLE `flow_def_version`
MODIFY COLUMN `act_def_id`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '定义ID' AFTER `id`;