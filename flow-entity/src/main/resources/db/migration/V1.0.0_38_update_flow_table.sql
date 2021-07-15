ALTER TABLE `flow_task_push`
  ADD COLUMN `new_task_auto`  TINYINT(1) NULL  COMMENT '推送的待办是否为自动执行' AFTER `flow_task_id`;

ALTER TABLE `flow_task_push`
  ADD COLUMN `approve_status` VARCHAR(20) NULL COMMENT '推送的已办审批状态' AFTER `new_task_auto`;