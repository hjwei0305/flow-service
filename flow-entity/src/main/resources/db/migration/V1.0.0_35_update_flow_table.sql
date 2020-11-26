CREATE TABLE `flow_disagree_reason` (
`id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL ,
`flow_type_id`  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联流程类型ID' ,
`code`  varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原因code' ,
`name`  varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原因简称' ,
`depict`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '原因描述' ,
`rank`  int(11) NULL DEFAULT NULL COMMENT '排序' ,
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

