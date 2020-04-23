ALTER TABLE `flow_task`
ADD COLUMN `executor_org_id`  varchar(36) NULL DEFAULT NULL COMMENT '执行人组织机构ID' AFTER `executor_account`,
ADD COLUMN `executor_org_code`  varchar(36) NULL DEFAULT NULL COMMENT '执行人组织机构code' AFTER `executor_org_id`,
ADD COLUMN `executor_org_name`  varchar(255) NULL DEFAULT NULL COMMENT '执行人组织机构名称' AFTER `executor_org_code`,
ADD COLUMN `owner_org_id`  varchar(36) NULL DEFAULT NULL COMMENT '拥有者组织机构ID' AFTER `owner_name`,
ADD COLUMN `owner_org_code`  varchar(36) NULL DEFAULT NULL COMMENT '拥有者组织机构code' AFTER `owner_org_id`,
ADD COLUMN `owner_org_name`  varchar(255) NULL DEFAULT NULL COMMENT '拥有者组织机构名称' AFTER `owner_org_code`;


ALTER TABLE `flow_history`
ADD COLUMN `owner_org_id`  varchar(36) NULL DEFAULT NULL COMMENT '拥有者组织机构ID' AFTER `owner_account`,
ADD COLUMN `owner_org_code`  varchar(36) NULL DEFAULT NULL COMMENT '拥有者组织机构code' AFTER `owner_org_id`,
ADD COLUMN `owner_org_name`  varchar(255) NULL DEFAULT NULL COMMENT '拥有者组织机构名称' AFTER `owner_org_code`,
ADD COLUMN `executor_org_id`  varchar(36) NULL DEFAULT NULL COMMENT '执行人组织机构ID' AFTER `executor_account`,
ADD COLUMN `executor_org_code`  varchar(36) NULL DEFAULT NULL COMMENT '执行人组织机构code' AFTER `executor_org_id`,
ADD COLUMN `executor_org_name`  varchar(255) NULL DEFAULT NULL COMMENT '执行人组织机构名称' AFTER `executor_org_code`;



ALTER TABLE `task_make_over_power`
ADD COLUMN `power_user_org_id`  varchar(36) NULL DEFAULT NULL COMMENT '被授权人组织机构id' AFTER `power_user_name`,
ADD COLUMN `power_user_org_code`  varchar(36) NULL DEFAULT NULL COMMENT '被授权人组织机构code' AFTER `power_user_org_id`,
ADD COLUMN `power_user_org_name`  varchar(255) NULL DEFAULT NULL COMMENT '被授权人组织机构名称' AFTER `power_user_org_code`;


