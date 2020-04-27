ALTER TABLE `task_make_over_power`
ADD COLUMN `make_over_power_type`  varchar(20) NULL DEFAULT 'sameToSee' COMMENT '授权类型' AFTER `user_name`,
ADD COLUMN `app_module_id`  varchar(36) NULL DEFAULT NULL COMMENT '应用模块ID' AFTER `make_over_power_type`,
ADD COLUMN `app_module_name`  varchar(30) NULL DEFAULT NULL COMMENT '应用模块名称' AFTER `app_module_id`,
ADD COLUMN `business_model_id`  varchar(36) NULL DEFAULT NULL COMMENT '业务实体ID' AFTER `app_module_name`,
ADD COLUMN `business_model_name`  varchar(80) NULL DEFAULT NULL COMMENT '业务实体名称' AFTER `business_model_id`,
ADD COLUMN `flow_type_id`  varchar(36) NULL DEFAULT NULL COMMENT '流程类型ID' AFTER `business_model_name`,
ADD COLUMN `flow_type_name`  varchar(80) NULL DEFAULT NULL COMMENT '流程类型名称' AFTER `flow_type_id`;