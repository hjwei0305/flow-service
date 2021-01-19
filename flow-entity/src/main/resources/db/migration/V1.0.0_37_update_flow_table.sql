ALTER TABLE `flow_def_version`
ADD COLUMN `timing`  int(11) NULL DEFAULT 0 COMMENT '流程额定工时' AFTER `flow_defination_status`;

ALTER TABLE `flow_def_version`
ADD COLUMN `early_warning_time`  int(11) NULL DEFAULT 0 COMMENT '提前预警时间' AFTER `timing`;

ALTER TABLE `flow_task`
ADD COLUMN `timing`  double NULL DEFAULT 0 COMMENT '任务额定工时' AFTER `execute_time`;

ALTER TABLE `flow_history`
ADD COLUMN `timing`  double NULL DEFAULT 0 COMMENT '任务额定工时' AFTER `flow_execute_status`;

